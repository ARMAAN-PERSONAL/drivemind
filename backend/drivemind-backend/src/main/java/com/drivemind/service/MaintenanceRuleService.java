package com.drivemind.service;

import com.drivemind.dto.GeneratedRuleRequest;
import com.drivemind.model.Car;
import com.drivemind.model.MaintenanceRule;
import com.drivemind.repository.CarRepository;
import com.drivemind.repository.MaintenanceRuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaintenanceRuleService {

    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final CarRepository carRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String openAiModel;

    public MaintenanceRuleService(MaintenanceRuleRepository maintenanceRuleRepository,
                                  CarRepository carRepository,
                                  ObjectMapper objectMapper) {
        this.maintenanceRuleRepository = maintenanceRuleRepository;
        this.carRepository = carRepository;
        this.objectMapper = objectMapper;
    }

    public MaintenanceRule addRule(Long carId, MaintenanceRule rule) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        rule.setCar(car);
        return maintenanceRuleRepository.save(rule);
    }

    public List<MaintenanceRule> getRulesForCar(Long carId) {
        return maintenanceRuleRepository.findByCarIdOrderByServiceTypeAsc(carId);
    }

    public void deleteRule(Long ruleId) {
        maintenanceRuleRepository.deleteById(ruleId);
    }

    public MaintenanceRule getRuleById(Long ruleId) {
        return maintenanceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
    }

    public Map<String, Object> generateStarterRules(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        String maintenanceProfile = determineMaintenanceProfile(car);

        List<MaintenanceRule> existingRules = maintenanceRuleRepository.findByCarIdOrderByServiceTypeAsc(carId);
        Set<String> existingServiceTypes = existingRules.stream()
                .map(rule -> normalize(rule.getServiceType()))
                .collect(Collectors.toSet());

        List<GeneratedRuleRequest> generatedRules = generateRulesWithAi(car, maintenanceProfile);

        int addedCount = 0;

        for (GeneratedRuleRequest generated : generatedRules) {
            if (generated.getServiceType() == null || generated.getServiceType().isBlank()) {
                continue;
            }

            String normalizedType = normalize(generated.getServiceType());

            if (existingServiceTypes.contains(normalizedType)) {
                continue;
            }

            Integer intervalKm = generated.getIntervalKm() != null && generated.getIntervalKm() > 0
                    ? generated.getIntervalKm()
                    : 10000;

            MaintenanceRule rule = MaintenanceRule.builder()
                    .serviceType(generated.getServiceType().trim())
                    .intervalKm(intervalKm)
                    .description(generated.getDescription() != null ? generated.getDescription().trim() : "")
                    .car(car)
                    .build();

            maintenanceRuleRepository.save(rule);
            existingServiceTypes.add(normalizedType);
            addedCount++;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("addedCount", addedCount);
        response.put("maintenanceProfile", maintenanceProfile);
        return response;
    }

    private List<GeneratedRuleRequest> generateRulesWithAi(Car car, String maintenanceProfile) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return buildFallbackRules(maintenanceProfile);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            String input = """
                    Generate starter maintenance rules for this vehicle using its maintenance profile.

                    Vehicle:
                    - Year: %s
                    - Make: %s
                    - Model: %s
                    - Trim: %s
                    - Current mileage: %s km

                    Maintenance profile:
                    - %s

                    Important rules:
                    - These are starter maintenance rules for a vehicle ownership app.
                    - Do not invent manufacturer-specific dealer schedules.
                    - Use broadly safe, realistic intervals in kilometers.
                    - The maintenance profile MUST change the interval emphasis.
                    - Sport-oriented and high-mileage vehicles should generally have tighter inspection/service intervals.
                    - Utility vehicles and SUVs can have different emphasis than commuter sedans.
                    - Return 6 to 8 rules.
                    - Keep descriptions short and owner-friendly.
                    - Return VALID JSON only.
                    - No markdown.
                    - No code fences.

                    Include common items like:
                    - Oil Change
                    - Tire Rotation
                    - Brake Inspection
                    - Cabin Air Filter
                    - Engine Air Filter
                    - Battery Inspection
                    - Coolant Check
                    - Transmission Service
                    - Spark Plugs when reasonable

                    Exact JSON shape:
                    [
                      {
                        "serviceType": "Oil Change",
                        "intervalKm": 8000,
                        "description": "Routine engine oil and filter service."
                      }
                    ]
                    """.formatted(
                    safe(car.getYear()),
                    safe(car.getMake()),
                    safe(car.getModel()),
                    safe(car.getTrim()),
                    safe(car.getCurrentMileage()),
                    maintenanceProfile
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
                return buildFallbackRules(maintenanceProfile);
            }

            JsonNode parsed = objectMapper.readTree(outputText.asText());

            if (!parsed.isArray()) {
                return buildFallbackRules(maintenanceProfile);
            }

            List<GeneratedRuleRequest> rules = new ArrayList<>();

            for (JsonNode node : parsed) {
                GeneratedRuleRequest rule = new GeneratedRuleRequest();
                rule.setServiceType(node.path("serviceType").asText("").trim());
                rule.setIntervalKm(node.path("intervalKm").isNumber() ? node.path("intervalKm").asInt() : null);
                rule.setDescription(node.path("description").asText("").trim());

                if (!rule.getServiceType().isBlank()) {
                    rules.add(rule);
                }
            }

            return rules.isEmpty() ? buildFallbackRules(maintenanceProfile) : rules;

        } catch (Exception ex) {
            return buildFallbackRules(maintenanceProfile);
        }
    }

    private List<GeneratedRuleRequest> buildFallbackRules(String maintenanceProfile) {
        return switch (maintenanceProfile) {
            case "Sport Compact" -> List.of(
                    buildRule("Oil Change", 6000, "Routine oil and filter service for a more performance-oriented driving profile."),
                    buildRule("Tire Rotation", 8000, "Rotate tires regularly to support even wear."),
                    buildRule("Brake Inspection", 10000, "Inspect pads, rotors, and braking condition more closely."),
                    buildRule("Cabin Air Filter", 18000, "Replace cabin air filter for cleaner airflow."),
                    buildRule("Engine Air Filter", 18000, "Inspect or replace engine air filter as needed."),
                    buildRule("Battery Inspection", 12000, "Check battery condition and terminal health."),
                    buildRule("Transmission Service", 45000, "Inspect or service transmission fluid based on condition."),
                    buildRule("Spark Plugs", 55000, "Inspect or replace spark plugs to maintain smooth performance.")
            );

            case "Crossover / SUV" -> List.of(
                    buildRule("Oil Change", 8000, "Routine engine oil and filter service."),
                    buildRule("Tire Rotation", 8000, "Rotate tires to support even wear on a utility-focused vehicle."),
                    buildRule("Brake Inspection", 12000, "Inspect brake pads, rotors, and overall brake condition."),
                    buildRule("Cabin Air Filter", 20000, "Replace cabin air filter for cleaner interior airflow."),
                    buildRule("Engine Air Filter", 18000, "Inspect or replace engine air filter as needed."),
                    buildRule("Battery Inspection", 15000, "Check battery condition and terminal health."),
                    buildRule("Coolant Check", 35000, "Inspect coolant condition and cooling system health."),
                    buildRule("Transmission Service", 50000, "Inspect or service transmission fluid based on condition.")
            );

            case "Truck / Utility" -> List.of(
                    buildRule("Oil Change", 7000, "Routine engine oil and filter service for utility-focused use."),
                    buildRule("Tire Rotation", 8000, "Rotate tires to support even wear under heavier-duty use."),
                    buildRule("Brake Inspection", 10000, "Inspect brake pads, rotors, and braking condition."),
                    buildRule("Engine Air Filter", 15000, "Inspect or replace engine air filter more often if conditions are harsher."),
                    buildRule("Battery Inspection", 12000, "Check battery condition and terminal health."),
                    buildRule("Coolant Check", 30000, "Inspect coolant condition and cooling system health."),
                    buildRule("Transmission Service", 45000, "Inspect or service transmission fluid based on condition."),
                    buildRule("Suspension Check", 20000, "Inspect suspension and steering components for wear.")
            );

            case "High-Mileage Daily Driver" -> List.of(
                    buildRule("Oil Change", 6000, "Routine oil and filter service with a shorter interval for an older or higher-mileage vehicle."),
                    buildRule("Tire Rotation", 9000, "Rotate tires to support even wear."),
                    buildRule("Brake Inspection", 10000, "Inspect pads, rotors, and brake condition more frequently."),
                    buildRule("Cabin Air Filter", 18000, "Replace cabin air filter for cleaner interior airflow."),
                    buildRule("Engine Air Filter", 18000, "Inspect or replace engine air filter as needed."),
                    buildRule("Battery Inspection", 10000, "Check battery condition and terminal health more regularly."),
                    buildRule("Coolant Check", 25000, "Inspect coolant condition and cooling system health."),
                    buildRule("Transmission Service", 40000, "Inspect or service transmission fluid based on condition.")
            );

            case "Family Sedan" -> List.of(
                    buildRule("Oil Change", 8000, "Routine engine oil and filter service."),
                    buildRule("Tire Rotation", 10000, "Rotate tires to support even wear."),
                    buildRule("Brake Inspection", 12000, "Inspect brake pads, rotors, and overall brake condition."),
                    buildRule("Cabin Air Filter", 20000, "Replace cabin air filter for cleaner interior airflow."),
                    buildRule("Engine Air Filter", 20000, "Inspect or replace engine air filter as needed."),
                    buildRule("Battery Inspection", 15000, "Check battery condition and terminal health."),
                    buildRule("Coolant Check", 40000, "Inspect coolant condition and cooling system health."),
                    buildRule("Transmission Service", 50000, "Inspect or service transmission fluid based on condition.")
            );

            default -> List.of(
                    buildRule("Oil Change", 8000, "Routine engine oil and filter service."),
                    buildRule("Tire Rotation", 10000, "Rotate tires to support even wear."),
                    buildRule("Brake Inspection", 12000, "Inspect brake pads, rotors, and overall brake condition."),
                    buildRule("Cabin Air Filter", 20000, "Replace cabin air filter for cleaner interior airflow."),
                    buildRule("Engine Air Filter", 20000, "Inspect or replace engine air filter as needed."),
                    buildRule("Battery Inspection", 15000, "Check battery condition and terminal health."),
                    buildRule("Coolant Check", 40000, "Inspect coolant condition and cooling system health."),
                    buildRule("Transmission Service", 50000, "Inspect or service transmission fluid based on condition.")
            );
        };
    }

    private String determineMaintenanceProfile(Car car) {
        String make = normalize(car.getMake());
        String model = normalize(car.getModel());
        String trim = normalize(car.getTrim());
        int mileage = car.getCurrentMileage() != null ? car.getCurrentMileage() : 0;

        if (mileage >= 160000) {
            return "High-Mileage Daily Driver";
        }

        if (model.contains("brz")
                || model.contains("wrx")
                || model.contains("gti")
                || model.contains("gli")
                || trim.contains("si")
                || trim.contains("sport")
                || trim.contains("type r")
                || trim.contains("st")) {
            return "Sport Compact";
        }

        if (model.contains("f-150")
                || model.contains("silverado")
                || model.contains("sierra")
                || model.contains("tundra")
                || model.contains("tacoma")
                || model.contains("ridgeline")
                || model.contains("ranger")
                || model.contains("canyon")
                || model.contains("frontier")) {
            return "Truck / Utility";
        }

        if (model.contains("cr-v")
                || model.contains("hr-v")
                || model.contains("rav4")
                || model.contains("highlander")
                || model.contains("pilot")
                || model.contains("passport")
                || model.contains("rogue")
                || model.contains("murano")
                || model.contains("pathfinder")
                || model.contains("cx-5")
                || model.contains("cx-30")
                || model.contains("cx-9")
                || model.contains("cx-90")
                || model.contains("x3")
                || model.contains("x5")
                || model.contains("q5")
                || model.contains("q7")
                || model.contains("tiguan")
                || model.contains("atlas")
                || model.contains("escape")
                || model.contains("explorer")
                || model.contains("edge")
                || model.contains("forester")
                || model.contains("outback")
                || model.contains("crosstrek")
                || model.contains("ascent")
                || model.contains("tucson")
                || model.contains("santa fe")
                || model.contains("palisade")
                || model.contains("sportage")
                || model.contains("sorento")
                || model.contains("telluride")) {
            return "Crossover / SUV";
        }

        if (model.contains("accord")
                || model.contains("camry")
                || model.contains("sonata")
                || model.contains("altima")
                || model.contains("malibu")
                || model.contains("passat")
                || model.contains("es")
                || model.contains("ls")
                || model.contains("a6")
                || model.contains("5 series")) {
            return "Family Sedan";
        }

        return "Commuter Sedan";
    }

    private GeneratedRuleRequest buildRule(String serviceType, Integer intervalKm, String description) {
        GeneratedRuleRequest rule = new GeneratedRuleRequest();
        rule.setServiceType(serviceType);
        rule.setIntervalKm(intervalKm);
        rule.setDescription(description);
        return rule;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}