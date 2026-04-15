package com.drivemind.service;

import com.drivemind.dto.VehicleSpotlightResponse;
import com.drivemind.dto.VinResponse;
import com.drivemind.model.Car;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class VehicleSpotlightService {

    private final CarService carService;
    private final VinService vinService;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String openAiModel;

    public VehicleSpotlightService(CarService carService,
                                   VinService vinService,
                                   ObjectMapper objectMapper) {
        this.carService = carService;
        this.vinService = vinService;
        this.objectMapper = objectMapper;
    }

    public VehicleSpotlightResponse generateVehicleSpotlight(Long carId, String email) {
        Car car = carService.getCarsByUser(email).stream()
                .filter(c -> Objects.equals(c.getId(), carId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Car not found"));

        VinResponse vinData = vinService.decodeVin(car.getVin());

        String vehicleName = buildVehicleName(car, vinData);
        String imageQuery = buildImageQuery(car, vinData);
        String imageSearchUrl = buildImageSearchUrl(imageQuery);

        String fallbackSummary = buildFallbackSummary(car, vinData);
        List<String> fallbackFacts = buildFallbackFacts(car, vinData);
        String fallbackPowertrain = buildFallbackPowertrainNote(vinData);

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return VehicleSpotlightResponse.builder()
                    .summary(fallbackSummary)
                    .funFacts(fallbackFacts)
                    .powertrainNote(fallbackPowertrain)
                    .imageQuery(imageQuery)
                    .imageSearchUrl(imageSearchUrl)
                    .aiGenerated(false)
                    .build();
        }

        try {
            SpotlightPayload payload = callOpenAi(vehicleName, car, vinData, imageQuery);

            return VehicleSpotlightResponse.builder()
                    .summary(payload.summary())
                    .funFacts(payload.funFacts())
                    .powertrainNote(payload.powertrainNote())
                    .imageQuery(payload.imageQuery() != null && !payload.imageQuery().isBlank()
                            ? payload.imageQuery()
                            : imageQuery)
                    .imageSearchUrl(buildImageSearchUrl(
                            payload.imageQuery() != null && !payload.imageQuery().isBlank()
                                    ? payload.imageQuery()
                                    : imageQuery
                    ))
                    .aiGenerated(true)
                    .build();

        } catch (Exception ex) {
            return VehicleSpotlightResponse.builder()
                    .summary(fallbackSummary)
                    .funFacts(fallbackFacts)
                    .powertrainNote(fallbackPowertrain)
                    .imageQuery(imageQuery)
                    .imageSearchUrl(imageSearchUrl)
                    .aiGenerated(false)
                    .build();
        }
    }

    private SpotlightPayload callOpenAi(String vehicleName,
                                        Car car,
                                        VinResponse vinData,
                                        String imageQuery) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        String input = """
                Create a Vehicle Spotlight for this car.

                Vehicle:
                - Name: %s
                - Year: %s
                - Make: %s
                - Model: %s
                - Trim: %s
                - VIN: %s
                - Vehicle type: %s
                - Body type: %s
                - Drivetrain: %s
                - Fuel type: %s
                - Transmission: %s
                - Engine configuration: %s
                - Engine cylinders: %s
                - Engine displacement: %s
                - Engine power: %s
                - Engine description: %s

                Use web search when helpful for lightweight public facts.
                Do not invent exact performance numbers or unsupported trim claims.
                Keep the tone polished, useful, and product-friendly.

                Return VALID JSON only in this exact shape:
                {
                  "summary": "string",
                  "funFacts": ["string", "string", "string"],
                  "powertrainNote": "string",
                  "imageQuery": "string"
                }

                Rules:
                - summary must be 70 to 100 words
                - funFacts must contain exactly 3 short facts
                - powertrainNote must be 1 to 2 sentences in simple English
                - imageQuery must be a good search query for similar real-world vehicle photos
                - no markdown
                - no extra keys
                - no code fences

                Suggested image query seed: %s
                """.formatted(
                vehicleName,
                safe(car.getYear()),
                safe(firstNonBlank(vinData.getMake(), car.getMake())),
                safe(firstNonBlank(vinData.getModel(), car.getModel())),
                safe(firstNonBlank(vinData.getTrim(), car.getTrim())),
                safe(car.getVin()),
                safe(vinData.getVehicleType()),
                safe(vinData.getBodyType()),
                safe(vinData.getDrivetrain()),
                safe(vinData.getFuelType()),
                safe(vinData.getTransmission()),
                safe(vinData.getEngineConfiguration()),
                safe(vinData.getEngineCylinders()),
                safe(vinData.getEngineDisplacement()),
                safe(vinData.getEnginePower()),
                safe(vinData.getEngineDescription()),
                imageQuery
        );

        Map<String, Object> webSearchTool = new HashMap<>();
        webSearchTool.put("type", "web_search_preview");

        Map<String, Object> body = new HashMap<>();
        body.put("model", openAiModel);
        body.put("input", input);
        body.put("tools", List.of(webSearchTool));

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

        String summary = parsed.path("summary").asText("");
        String powertrainNote = parsed.path("powertrainNote").asText("");
        String returnedImageQuery = parsed.path("imageQuery").asText(imageQuery);

        List<String> funFacts = new ArrayList<>();
        JsonNode factsNode = parsed.path("funFacts");
        if (factsNode.isArray()) {
            for (JsonNode fact : factsNode) {
                if (!fact.asText().isBlank()) {
                    funFacts.add(fact.asText());
                }
            }
        }

        if (summary.isBlank() || powertrainNote.isBlank() || funFacts.isEmpty()) {
            throw new RuntimeException("Incomplete AI spotlight response");
        }

        return new SpotlightPayload(summary, funFacts, powertrainNote, returnedImageQuery);
    }

    private String buildVehicleName(Car car, VinResponse vinData) {
        return (
                safe(firstNonBlank(vinData.getYear() != null ? String.valueOf(vinData.getYear()) : null,
                        car.getYear() != null ? String.valueOf(car.getYear()) : null)) + " " +
                        safe(firstNonBlank(vinData.getMake(), car.getMake())) + " " +
                        safe(firstNonBlank(vinData.getModel(), car.getModel())) + " " +
                        safe(firstNonBlank(vinData.getTrim(), car.getTrim()))
        ).trim().replaceAll("\\s+", " ");
    }

    private String buildImageQuery(Car car, VinResponse vinData) {
        return (
                safe(firstNonBlank(vinData.getYear() != null ? String.valueOf(vinData.getYear()) : null,
                        car.getYear() != null ? String.valueOf(car.getYear()) : null)) + " " +
                        safe(firstNonBlank(vinData.getMake(), car.getMake())) + " " +
                        safe(firstNonBlank(vinData.getModel(), car.getModel())) + " " +
                        safe(firstNonBlank(vinData.getTrim(), car.getTrim())) + " " +
                        safe(firstNonBlank(vinData.getBodyType(), "car"))
        ).trim().replaceAll("\\s+", " ");
    }

    private String buildImageSearchUrl(String imageQuery) {
        String encoded = URLEncoder.encode(imageQuery, StandardCharsets.UTF_8);
        return "https://www.google.com/search?tbm=isch&q=" + encoded;
    }

    private String buildFallbackSummary(Car car, VinResponse vinData) {
        String vehicleName = buildVehicleName(car, vinData);

        return vehicleName + " is a tracked vehicle in DriveMind with a profile centered around practical ownership, core vehicle identity, and powertrain interpretation. Based on the available decoded details, this vehicle can be summarized through its body style, drivetrain, and engine layout to give the owner a more useful sense of what the car is built for in everyday use. The spotlight combines saved vehicle data with a broader vehicle-style overview for a cleaner at-a-glance understanding.";
    }

    private List<String> buildFallbackFacts(Car car, VinResponse vinData) {
        List<String> facts = new ArrayList<>();

        if (vinData.getDrivetrain() != null && !vinData.getDrivetrain().isBlank()) {
            facts.add(vinData.getDrivetrain() + " layout usually shapes how the vehicle feels in everyday road use.");
        }

        if (vinData.getBodyType() != null && !vinData.getBodyType().isBlank()) {
            facts.add(vinData.getBodyType() + " body styles are often chosen for their balance of space, usability, and road presence.");
        }

        if (vinData.getFuelType() != null && !vinData.getFuelType().isBlank()) {
            facts.add(vinData.getFuelType() + " powertrains influence both running character and ownership expectations.");
        }

        while (facts.size() < 3) {
            facts.add("Vehicle spotlight details become stronger as decoded VIN and ownership data are available together.");
        }

        return facts.subList(0, 3);
    }

    private String buildFallbackPowertrainNote(VinResponse vinData) {
        if (vinData.getEngineDescription() != null && !vinData.getEngineDescription().isBlank()) {
            return vinData.getEngineDescription() + " usually points to the overall driving character and ownership feel more than just raw numbers.";
        }

        return "The powertrain setup shapes how the vehicle delivers daily usability, efficiency expectations, and general driving character.";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private record SpotlightPayload(
            String summary,
            List<String> funFacts,
            String powertrainNote,
            String imageQuery
    ) {}
}