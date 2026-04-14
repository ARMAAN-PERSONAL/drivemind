package com.drivemind.service;

import com.drivemind.dto.AiInsightResponse;
import com.drivemind.model.Car;
import com.drivemind.model.MaintenanceLog;
import com.drivemind.model.MaintenanceRule;
import com.drivemind.repository.MaintenanceLogRepository;
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
public class AiInsightService {

    private final CarService carService;
    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String openAiModel;

    public AiInsightService(CarService carService,
                            MaintenanceRuleRepository maintenanceRuleRepository,
                            MaintenanceLogRepository maintenanceLogRepository,
                            ObjectMapper objectMapper) {
        this.carService = carService;
        this.maintenanceRuleRepository = maintenanceRuleRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.objectMapper = objectMapper;
    }

    public AiInsightResponse generateCarInsight(Long carId, String email) {
        Car car = carService.getCarsByUser(email).stream()
                .filter(c -> Objects.equals(c.getId(), carId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Car not found"));

        List<MaintenanceRule> rules = maintenanceRuleRepository.findByCarIdOrderByServiceTypeAsc(carId);
        List<MaintenanceLog> logs = maintenanceLogRepository.findByCarId(carId);

        int currentMileage = car.getCurrentMileage() != null ? car.getCurrentMileage() : 0;

        int overdueCount = 0;
        int dueSoonCount = 0;
        List<String> overdueItems = new ArrayList<>();
        List<String> dueSoonItems = new ArrayList<>();
        List<String> recentItems = getRecentMaintenanceItems(logs);

        for (MaintenanceRule rule : rules) {
            MaintenanceLog latestMatchingLog = findLatestMatchingLog(rule, logs);

            if (latestMatchingLog == null || latestMatchingLog.getMileage() == null || rule.getIntervalKm() == null) {
                continue;
            }

            int nextDueMileage = latestMatchingLog.getMileage() + rule.getIntervalKm();
            int kmRemaining = nextDueMileage - currentMileage;

            if (kmRemaining < 0) {
                overdueCount++;
                overdueItems.add(rule.getServiceType());
            } else if (kmRemaining <= 1000) {
                dueSoonCount++;
                dueSoonItems.add(rule.getServiceType());
            }
        }

        String overallStatus = resolveOverallStatus(overdueCount, dueSoonCount);
        int healthScore = calculateHealthScore(overdueCount, dueSoonCount, rules.size());

        String fallbackSummary = buildFallbackSummary(
                car, currentMileage, overallStatus, healthScore,
                overdueItems, dueSoonItems, recentItems, rules.size()
        );

        String fallbackRecommendation = buildRecommendation(overallStatus, overdueItems, dueSoonItems);

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return AiInsightResponse.builder()
                    .summary(fallbackSummary)
                    .recommendation(fallbackRecommendation)
                    .overallStatus(overallStatus)
                    .healthScore(healthScore)
                    .aiGenerated(false)
                    .build();
        }

        try {
            String aiSummary = callOpenAi(
                    car,
                    currentMileage,
                    rules.size(),
                    overdueItems,
                    dueSoonItems,
                    recentItems,
                    overallStatus,
                    healthScore
            );

            return AiInsightResponse.builder()
                    .summary(aiSummary)
                    .recommendation(fallbackRecommendation)
                    .overallStatus(overallStatus)
                    .healthScore(healthScore)
                    .aiGenerated(true)
                    .build();

        } catch (Exception ex) {
            return AiInsightResponse.builder()
                    .summary(fallbackSummary)
                    .recommendation(fallbackRecommendation)
                    .overallStatus(overallStatus)
                    .healthScore(healthScore)
                    .aiGenerated(false)
                    .build();
        }
    }

    private MaintenanceLog findLatestMatchingLog(MaintenanceRule rule, List<MaintenanceLog> logs) {
        String ruleType = normalize(rule.getServiceType());

        return logs.stream()
                .filter(log -> normalize(log.getType()).contains(ruleType)
                        || ruleType.contains(normalize(log.getType())))
                .filter(log -> log.getMileage() != null)
                .max(Comparator.comparing(MaintenanceLog::getMileage))
                .orElse(null);
    }

    private List<String> getRecentMaintenanceItems(List<MaintenanceLog> logs) {
        return logs.stream()
                .sorted((a, b) -> {
                    if (a.getServiceDate() == null && b.getServiceDate() == null) return 0;
                    if (a.getServiceDate() == null) return 1;
                    if (b.getServiceDate() == null) return -1;
                    return b.getServiceDate().compareTo(a.getServiceDate());
                })
                .limit(3)
                .map(log -> {
                    String type = log.getType() != null ? log.getType() : "Service";
                    String mileage = log.getMileage() != null ? log.getMileage() + " km" : "unknown mileage";
                    return type + " at " + mileage;
                })
                .collect(Collectors.toList());
    }

    private String resolveOverallStatus(int overdueCount, int dueSoonCount) {
        if (overdueCount > 0) return "OVERDUE";
        if (dueSoonCount > 0) return "DUE_SOON";
        return "HEALTHY";
    }

    private int calculateHealthScore(int overdueCount, int dueSoonCount, int totalRules) {
        int score = 100;
        score -= overdueCount * 20;
        score -= dueSoonCount * 8;

        if (totalRules == 0) {
            score = Math.min(score, 85);
        }

        return Math.max(score, 40);
    }

    private String buildFallbackSummary(Car car,
                                        int currentMileage,
                                        String overallStatus,
                                        int healthScore,
                                        List<String> overdueItems,
                                        List<String> dueSoonItems,
                                        List<String> recentItems,
                                        int ruleCount) {

        String vehicleName = buildVehicleName(car);

        StringBuilder sb = new StringBuilder();
        sb.append(vehicleName)
                .append(" is currently tracked at ")
                .append(currentMileage)
                .append(" km with a health score of ")
                .append(healthScore)
                .append("/100. ");

        if ("OVERDUE".equals(overallStatus)) {
            sb.append("The vehicle has overdue maintenance attention based on tracked service intervals. ");
        } else if ("DUE_SOON".equals(overallStatus)) {
            sb.append("The vehicle is in generally decent condition, but one or more service items are approaching their due interval. ");
        } else {
            sb.append("The vehicle currently appears healthy based on tracked maintenance intervals. ");
        }

        sb.append("DriveMind is monitoring ")
                .append(ruleCount)
                .append(" maintenance rule")
                .append(ruleCount == 1 ? "" : "s")
                .append(" for this vehicle. ");

        if (!recentItems.isEmpty()) {
            sb.append("Recent recorded work includes ")
                    .append(String.join(", ", recentItems))
                    .append(". ");
        }

        if (!overdueItems.isEmpty()) {
            sb.append("Overdue items: ")
                    .append(String.join(", ", overdueItems))
                    .append(". ");
        } else if (!dueSoonItems.isEmpty()) {
            sb.append("Upcoming items: ")
                    .append(String.join(", ", dueSoonItems))
                    .append(". ");
        }

        return sb.toString().trim();
    }

    private String buildRecommendation(String overallStatus,
                                       List<String> overdueItems,
                                       List<String> dueSoonItems) {
        if ("OVERDUE".equals(overallStatus) && !overdueItems.isEmpty()) {
            return "Prioritize " + overdueItems.get(0) + " first, then review the remaining due items to restore a healthier service position.";
        }

        if ("DUE_SOON".equals(overallStatus) && !dueSoonItems.isEmpty()) {
            return "Plan the next service for " + dueSoonItems.get(0) + " soon to prevent it from slipping into overdue status.";
        }

        return "Keep logging mileage, fuel, and service activity consistently to maintain an accurate vehicle health picture.";
    }

    private String callOpenAi(Car car,
                              int currentMileage,
                              int ruleCount,
                              List<String> overdueItems,
                              List<String> dueSoonItems,
                              List<String> recentItems,
                              String overallStatus,
                              int healthScore) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        String vehicleName = buildVehicleName(car);

        String input = """
                Vehicle: %s
                Current mileage: %d km
                Maintenance rules tracked: %d
                Overall status: %s
                Health score: %d/100
                Overdue items: %s
                Due soon items: %s
                Recent maintenance: %s

                Write one short professional paragraph for a vehicle insights screen.
                Use only the supplied facts.
                Do not invent failures, symptoms, or repairs.
                Keep it under 90 words.
                End with one practical next-step sentence.
                """.formatted(
                vehicleName,
                currentMileage,
                ruleCount,
                overallStatus,
                healthScore,
                overdueItems.isEmpty() ? "None" : String.join(", ", overdueItems),
                dueSoonItems.isEmpty() ? "None" : String.join(", ", dueSoonItems),
                recentItems.isEmpty() ? "None recorded" : String.join(", ", recentItems)
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

        return outputText.asText().trim();
    }

    private String buildVehicleName(Car car) {
        String year = car.getYear() != null ? String.valueOf(car.getYear()) : "";
        String make = car.getMake() != null ? car.getMake() : "";
        String model = car.getModel() != null ? car.getModel() : "";
        String trim = car.getTrim() != null ? car.getTrim() : "";

        return (year + " " + make + " " + model + " " + trim).trim().replaceAll("\\s+", " ");
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase().trim();
    }
}