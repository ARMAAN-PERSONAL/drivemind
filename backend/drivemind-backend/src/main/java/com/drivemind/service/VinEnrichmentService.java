package com.drivemind.service;

import com.drivemind.dto.EnrichedVinResponse;
import com.drivemind.dto.VinResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class VinEnrichmentService {

    private final VinService vinService;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String openAiModel;

    public VinEnrichmentService(VinService vinService, ObjectMapper objectMapper) {
        this.vinService = vinService;
        this.objectMapper = objectMapper;
    }

    public EnrichedVinResponse decodeAndEnrich(String vin) {
        VinResponse raw = vinService.decodeVin(vin);
        EnrichedVinResponse fallback = buildFallback(raw);

        if (raw == null || "FAILED".equalsIgnoreCase(raw.getStatus())) {
            return fallback;
        }

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return fallback;
        }

        try {
            AiVinPayload ai = callOpenAi(raw);
            return mergeRawWithAi(raw, ai);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private EnrichedVinResponse buildFallback(VinResponse raw) {
        return EnrichedVinResponse.builder()
                .make(defaultText(raw.getMake(), "Unknown make"))
                .model(defaultText(raw.getModel(), "Unknown model"))
                .year(raw.getYear())
                .trim(defaultText(raw.getTrim(), "Standard / Unspecified trim"))
                .color(defaultText(raw.getColor(), "Color not decoded"))
                .drivetrain(defaultText(raw.getDrivetrain(), "Drivetrain not clearly decoded"))
                .vehicleType(defaultText(raw.getVehicleType(), "Passenger vehicle"))
                .bodyType(defaultText(raw.getBodyType(), "Body style not clearly decoded"))
                .engineConfiguration(defaultText(raw.getEngineConfiguration(), "Engine configuration not clearly decoded"))
                .engineCylinders(raw.getEngineCylinders())
                .engineDisplacement(raw.getEngineDisplacement())
                .enginePower(raw.getEnginePower())
                .fuelType(defaultText(raw.getFuelType(), "Fuel type not clearly decoded"))
                .transmission(defaultText(raw.getTransmission(), "Transmission not clearly decoded"))
                .engineDescription(buildFallbackEngineDescription(raw))
                .vehicleCategory(buildFallbackCategory(raw))
                .shortSummary(buildFallbackSummary(raw))
                .confidenceLabel(calculateConfidence(raw))
                .hasCoreInfo(raw.isHasCoreInfo())
                .hasEngineInfo(raw.isHasEngineInfo())
                .aiGenerated(false)
                .dataSource(defaultText(raw.getDataSource(), "SYSTEM"))
                .status(defaultText(raw.getStatus(), "PARTIAL"))
                .message(defaultText(raw.getMessage(), "Vehicle data normalized with safe fallback values"))
                .build();
    }

    private EnrichedVinResponse mergeRawWithAi(VinResponse raw, AiVinPayload ai) {
        return EnrichedVinResponse.builder()
                .make(preferRaw(raw.getMake(), ai.make(), "Unknown make"))
                .model(preferRaw(raw.getModel(), ai.model(), "Unknown model"))
                .year(raw.getYear() != null ? raw.getYear() : ai.year())
                .trim(preferRaw(raw.getTrim(), ai.trim(), "Standard / Unspecified trim"))
                .color(preferRaw(raw.getColor(), ai.color(), "Color not decoded"))
                .drivetrain(preferRaw(raw.getDrivetrain(), ai.drivetrain(), "Drivetrain not clearly decoded"))
                .vehicleType(preferRaw(raw.getVehicleType(), ai.vehicleType(), "Passenger vehicle"))
                .bodyType(preferRaw(raw.getBodyType(), ai.bodyType(), "Body style not clearly decoded"))
                .engineConfiguration(preferRaw(raw.getEngineConfiguration(), ai.engineConfiguration(), "Engine configuration not clearly decoded"))
                .engineCylinders(raw.getEngineCylinders() != null ? raw.getEngineCylinders() : ai.engineCylinders())
                .engineDisplacement(raw.getEngineDisplacement() != null ? raw.getEngineDisplacement() : ai.engineDisplacement())
                .enginePower(raw.getEnginePower() != null ? raw.getEnginePower() : ai.enginePower())
                .fuelType(preferRaw(raw.getFuelType(), ai.fuelType(), "Fuel type not clearly decoded"))
                .transmission(preferRaw(raw.getTransmission(), ai.transmission(), "Transmission not clearly decoded"))
                .engineDescription(preferUsefulEngineDescription(raw.getEngineDescription(), ai.engineDescription()))
                .vehicleCategory(defaultText(ai.vehicleCategory(), buildFallbackCategory(raw)))
                .shortSummary(defaultText(ai.shortSummary(), buildFallbackSummary(raw)))
                .confidenceLabel(calculateConfidence(raw))
                .hasCoreInfo(raw.isHasCoreInfo())
                .hasEngineInfo(raw.isHasEngineInfo())
                .aiGenerated(true)
                .dataSource(defaultText(raw.getDataSource(), "SYSTEM") + " + AI")
                .status(defaultText(raw.getStatus(), "PARTIAL"))
                .message(defaultText(raw.getMessage(), "Vehicle data enriched with AI normalization"))
                .build();
    }

    private AiVinPayload callOpenAi(VinResponse raw) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String input = """
                You are enriching VIN decode output for a vehicle management app.

                Use only the supplied facts.
                Do not invent exact factory specs, trim claims, or performance numbers.
                If a field is missing, fill it only with a safe, generic, owner-friendly value.
                Prefer broad but useful language over fake precision.

                Raw decoded data:
                make=%s
                model=%s
                year=%s
                trim=%s
                color=%s
                drivetrain=%s
                vehicleType=%s
                bodyType=%s
                engineConfiguration=%s
                engineCylinders=%s
                engineDisplacement=%s
                enginePower=%s
                fuelType=%s
                transmission=%s
                engineDescription=%s
                status=%s
                message=%s

                Return VALID JSON only with exactly these keys:
                {
                  "make": "string",
                  "model": "string",
                  "year": 0,
                  "trim": "string",
                  "color": "string",
                  "drivetrain": "string",
                  "vehicleType": "string",
                  "bodyType": "string",
                  "engineConfiguration": "string",
                  "engineCylinders": 0,
                  "engineDisplacement": 0.0,
                  "enginePower": 0,
                  "fuelType": "string",
                  "transmission": "string",
                  "engineDescription": "string",
                  "vehicleCategory": "string",
                  "shortSummary": "string"
                }

                shortSummary rules:
                - 1 sentence
                - product-like tone
                - useful for a Cars page preview

                engineDescription rules:
                - simple English
                - never blank
                - okay to be generic if exact engine details are missing
                """.formatted(
                safe(raw.getMake()),
                safe(raw.getModel()),
                safe(raw.getYear()),
                safe(raw.getTrim()),
                safe(raw.getColor()),
                safe(raw.getDrivetrain()),
                safe(raw.getVehicleType()),
                safe(raw.getBodyType()),
                safe(raw.getEngineConfiguration()),
                safe(raw.getEngineCylinders()),
                safe(raw.getEngineDisplacement()),
                safe(raw.getEnginePower()),
                safe(raw.getFuelType()),
                safe(raw.getTransmission()),
                safe(raw.getEngineDescription()),
                safe(raw.getStatus()),
                safe(raw.getMessage())
        );

        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiModel);
        body.put("input", input);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.openai.com/v1/responses",
                HttpMethod.POST,
                entity,
                String.class
        );

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode outputText = root.path("output").get(0)
                .path("content").get(0)
                .path("text");

        if (outputText.isMissingNode() || outputText.asText().isBlank()) {
            throw new RuntimeException("Empty AI response");
        }

        JsonNode parsed = objectMapper.readTree(outputText.asText());

        return new AiVinPayload(
                parsed.path("make").asText(""),
                parsed.path("model").asText(""),
                parsed.path("year").isNumber() ? parsed.path("year").asInt() : null,
                parsed.path("trim").asText(""),
                parsed.path("color").asText(""),
                parsed.path("drivetrain").asText(""),
                parsed.path("vehicleType").asText(""),
                parsed.path("bodyType").asText(""),
                parsed.path("engineConfiguration").asText(""),
                parsed.path("engineCylinders").isNumber() ? parsed.path("engineCylinders").asInt() : null,
                parsed.path("engineDisplacement").isNumber() ? parsed.path("engineDisplacement").asDouble() : null,
                parsed.path("enginePower").isNumber() ? parsed.path("enginePower").asInt() : null,
                parsed.path("fuelType").asText(""),
                parsed.path("transmission").asText(""),
                parsed.path("engineDescription").asText(""),
                parsed.path("vehicleCategory").asText(""),
                parsed.path("shortSummary").asText("")
        );
    }

    private String preferUsefulEngineDescription(String raw, String ai) {
        if (raw != null && !raw.isBlank() && !"Engine info not available".equalsIgnoreCase(raw.trim())) {
            return raw;
        }
        return defaultText(ai, "Powertrain details are limited, but this vehicle can still be tracked normally in DriveMind.");
    }

    private String buildFallbackEngineDescription(VinResponse raw) {
        if (raw.getEngineDescription() != null && !raw.getEngineDescription().isBlank()
                && !"Engine info not available".equalsIgnoreCase(raw.getEngineDescription())) {
            return raw.getEngineDescription();
        }

        String fuel = raw.getFuelType() != null ? raw.getFuelType() : "standard";
        return "A " + fuel.toLowerCase() + "-powered setup with limited decoded engine detail, presented in a safe owner-friendly format.";
    }

    private String buildFallbackCategory(VinResponse raw) {
        if (raw.getBodyType() != null && raw.getBodyType().toLowerCase().contains("sedan")) {
            return "Daily-use sedan";
        }
        if (raw.getVehicleType() != null && raw.getVehicleType().toLowerCase().contains("multipurpose")) {
            return "Practical utility vehicle";
        }
        if (raw.getBodyType() != null && raw.getBodyType().toLowerCase().contains("sport utility")) {
            return "Utility-focused crossover or SUV";
        }
        return "General passenger vehicle";
    }

    private String buildFallbackSummary(VinResponse raw) {
        String year = raw.getYear() != null ? raw.getYear().toString() : "This";
        String make = defaultText(raw.getMake(), "vehicle");
        String model = defaultText(raw.getModel(), "");
        String trim = raw.getTrim() != null ? raw.getTrim() : "unspecified trim";

        return (year + " " + make + " " + model + " in " + trim +
                " is ready to be tracked in DriveMind with normalized vehicle details, safer fallback descriptions, and a decode profile designed to reduce missing-value issues in the app.")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String calculateConfidence(VinResponse raw) {
        int score = 0;
        if (raw.getMake() != null) score++;
        if (raw.getModel() != null) score++;
        if (raw.getYear() != null) score++;
        if (raw.getTrim() != null) score++;
        if (raw.getBodyType() != null) score++;
        if (raw.getDrivetrain() != null) score++;
        if (raw.getFuelType() != null) score++;
        if (raw.getTransmission() != null) score++;
        if (raw.getEngineDescription() != null && !"Engine info not available".equalsIgnoreCase(raw.getEngineDescription())) score += 2;

        if (score >= 8) return "HIGH";
        if (score >= 5) return "MEDIUM";
        return "LOW";
    }

    private String preferRaw(String raw, String ai, String fallback) {
        if (raw != null && !raw.isBlank()) return raw;
        if (ai != null && !ai.isBlank()) return ai;
        return fallback;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private record AiVinPayload(
            String make,
            String model,
            Integer year,
            String trim,
            String color,
            String drivetrain,
            String vehicleType,
            String bodyType,
            String engineConfiguration,
            Integer engineCylinders,
            Double engineDisplacement,
            Integer enginePower,
            String fuelType,
            String transmission,
            String engineDescription,
            String vehicleCategory,
            String shortSummary
    ) {}
}