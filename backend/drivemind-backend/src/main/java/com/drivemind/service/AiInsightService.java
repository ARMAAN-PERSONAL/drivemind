package com.drivemind.service;

import com.drivemind.dto.AiInsightResponse;
import com.drivemind.model.Car;
import com.drivemind.model.FuelLog;
import com.drivemind.model.MaintenanceLog;
import com.drivemind.model.MaintenanceRule;
import com.drivemind.repository.FuelLogRepository;
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
    private final FuelLogRepository fuelLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-5-mini}")
    private String openAiModel;

    public AiInsightService(CarService carService,
                            MaintenanceRuleRepository maintenanceRuleRepository,
                            MaintenanceLogRepository maintenanceLogRepository,
                            FuelLogRepository fuelLogRepository,
                            ObjectMapper objectMapper) {
        this.carService = carService;
        this.maintenanceRuleRepository = maintenanceRuleRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.fuelLogRepository = fuelLogRepository;
        this.objectMapper = objectMapper;
    }

    public AiInsightResponse generateCarInsight(Long carId, String email) {
        Car car = carService.getCarsByUser(email).stream()
                .filter(c -> Objects.equals(c.getId(), carId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Car not found"));

        List<MaintenanceRule> rules = maintenanceRuleRepository.findByCarIdOrderByServiceTypeAsc(carId);
        List<MaintenanceLog> maintenanceLogs = maintenanceLogRepository.findByCarId(carId);
        List<FuelLog> fuelLogs = fuelLogRepository.findByCarId(carId);

        int currentMileage = car.getCurrentMileage() != null ? car.getCurrentMileage() : 0;

        int overdueCount = 0;
        int dueSoonCount = 0;

        List<String> overdueItems = new ArrayList<>();
        List<String> dueSoonItems = new ArrayList<>();
        List<String> recentMaintenanceItems = getRecentMaintenanceItems(maintenanceLogs);

        for (MaintenanceRule rule : rules) {
            MaintenanceLog latestMatchingLog = findLatestMatchingLog(rule, maintenanceLogs);

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

        FuelSnapshot fuelSnapshot = buildFuelSnapshot(fuelLogs);

        String fallbackSummary = buildFallbackSummary(
                car,
                currentMileage,
                overallStatus,
                healthScore,
                overdueItems,
                dueSoonItems,
                recentMaintenanceItems,
                rules.size(),
                fuelSnapshot
        );

        String fallbackRecommendation = buildRecommendation(
                overallStatus,
                overdueItems,
                dueSoonItems,
                fuelSnapshot
        );

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return buildResponse(
                    fallbackSummary,
                    fallbackRecommendation,
                    overallStatus,
                    healthScore,
                    false,
                    fuelSnapshot
            );
        }

        try {
            String aiSummary = callOpenAi(
                    car,
                    currentMileage,
                    rules.size(),
                    overdueItems,
                    dueSoonItems,
                    recentMaintenanceItems,
                    overallStatus,
                    healthScore,
                    fuelSnapshot
            );

            return buildResponse(
                    aiSummary,
                    fallbackRecommendation,
                    overallStatus,
                    healthScore,
                    true,
                    fuelSnapshot
            );
        } catch (Exception ex) {
            return buildResponse(
                    fallbackSummary,
                    fallbackRecommendation,
                    overallStatus,
                    healthScore,
                    false,
                    fuelSnapshot
            );
        }
    }

    private AiInsightResponse buildResponse(String summary,
                                            String recommendation,
                                            String overallStatus,
                                            int healthScore,
                                            boolean aiGenerated,
                                            FuelSnapshot fuelSnapshot) {
        return AiInsightResponse.builder()
                .summary(summary)
                .recommendation(recommendation)
                .overallStatus(overallStatus)
                .healthScore(healthScore)
                .aiGenerated(aiGenerated)
                .fuelLogCount(fuelSnapshot.fuelLogCount)
                .totalFuelSpend(fuelSnapshot.totalFuelSpend)
                .averageFuelSpend(fuelSnapshot.averageFuelSpend)
                .averageFuelVolume(fuelSnapshot.averageFuelVolume)
                .latestFuelCost(fuelSnapshot.latestFuelCost)
                .latestFuelVolume(fuelSnapshot.latestFuelVolume)
                .latestFuelMileage(fuelSnapshot.latestFuelMileage)
                .build();
    }

    private FuelSnapshot buildFuelSnapshot(List<FuelLog> fuelLogs) {
        if (fuelLogs == null || fuelLogs.isEmpty()) {
            return new FuelSnapshot(0, 0.0, 0.0, 0.0, null, null, null);
        }

        List<FuelLog> sortedLogs = fuelLogs.stream()
                .sorted((a, b) -> {
                    if (a.getDate() == null && b.getDate() == null) return 0;
                    if (a.getDate() == null) return 1;
                    if (b.getDate() == null) return -1;
                    return b.getDate().compareTo(a.getDate());
                })
                .toList();

        double totalFuelSpend = fuelLogs.stream()
                .mapToDouble(log -> log.getPrice() != null ? log.getPrice() : 0.0)
                .sum();

        double totalFuelVolume = fuelLogs.stream()
                .mapToDouble(log -> log.getLiters() != null ? log.getLiters() : 0.0)
                .sum();

        int fuelLogCount = fuelLogs.size();

        Double averageFuelSpend = fuelLogCount > 0 ? round(totalFuelSpend / fuelLogCount) : 0.0;
        Double averageFuelVolume = fuelLogCount > 0 ? round(totalFuelVolume / fuelLogCount) : 0.0;

        FuelLog latestFuelLog = sortedLogs.get(0);

        return new FuelSnapshot(
                fuelLogCount,
                round(totalFuelSpend),
                averageFuelSpend,
                averageFuelVolume,
                latestFuelLog.getPrice() != null ? round(latestFuelLog.getPrice()) : null,
                latestFuelLog.getLiters(),
                latestFuelLog.getMileage()
        );
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
                                        List<String> recentMaintenanceItems,
                                        int ruleCount,
                                        FuelSnapshot fuelSnapshot) {

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

        if (!recentMaintenanceItems.isEmpty()) {
            sb.append("Recent recorded work includes ")
                    .append(String.join(", ", recentMaintenanceItems))
                    .append(". ");
        }

        if (fuelSnapshot.fuelLogCount > 0) {
            sb.append("Fuel tracking includes ")
                    .append(fuelSnapshot.fuelLogCount)
                    .append(" fill-up log")
                    .append(fuelSnapshot.fuelLogCount == 1 ? "" : "s")
                    .append(" with total recorded fuel spend of $")
                    .append(String.format("%.2f", fuelSnapshot.totalFuelSpend))
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
                                       List<String> dueSoonItems,
                                       FuelSnapshot fuelSnapshot) {
        if ("OVERDUE".equals(overallStatus) && !overdueItems.isEmpty()) {
            return "Prioritize " + overdueItems.get(0) + " first, then review the remaining due items to restore a healthier service position.";
        }

        if ("DUE_SOON".equals(overallStatus) && !dueSoonItems.isEmpty()) {
            return "Plan the next service for " + dueSoonItems.get(0) + " soon to prevent it from slipping into overdue status.";
        }

        if (fuelSnapshot.fuelLogCount >= 3) {
            return "Keep fuel tracking active alongside mileage and service logs so DriveMind can maintain a stronger ownership picture over time.";
        }

        return "Keep logging mileage, fuel, and service activity consistently to maintain an accurate vehicle health picture.";
    }

    private String callOpenAi(Car car,
                              int currentMileage,
                              int ruleCount,
                              List<String> overdueItems,
                              List<String> dueSoonItems,
                              List<String> recentMaintenanceItems,
                              String overallStatus,
                              int healthScore,
                              FuelSnapshot fuelSnapshot) throws Exception {

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
                Fuel logs tracked: %d
                Total fuel spend recorded: $%.2f
                Average fuel spend per fill: $%.2f
                Average fuel volume per fill: %.2f L
                Latest fuel entry cost: %s
                Latest fuel entry volume: %s
                Latest fuel entry mileage: %s

                Write one short professional vehicle ownership insight for an app insights screen.
                Use only the supplied facts.
                Mention both maintenance position and fuel tracking behavior if fuel data exists.
                Do not invent faults, failures, or repairs.
                Keep it under 110 words.
                End with one practical next-step sentence.
                """.formatted(
                vehicleName,
                currentMileage,
                ruleCount,
                overallStatus,
                healthScore,
                overdueItems.isEmpty() ? "None" : String.join(", ", overdueItems),
                dueSoonItems.isEmpty() ? "None" : String.join(", ", dueSoonItems),
                recentMaintenanceItems.isEmpty() ? "None recorded" : String.join(", ", recentMaintenanceItems),
                fuelSnapshot.fuelLogCount,
                fuelSnapshot.totalFuelSpend,
                fuelSnapshot.averageFuelSpend,
                fuelSnapshot.averageFuelVolume,
                fuelSnapshot.latestFuelCost != null ? "$" + String.format("%.2f", fuelSnapshot.latestFuelCost) : "None",
                fuelSnapshot.latestFuelVolume != null ? String.format("%.2f L", fuelSnapshot.latestFuelVolume) : "None",
                fuelSnapshot.latestFuelMileage != null ? fuelSnapshot.latestFuelMileage + " km" : "None"
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

    private Double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class FuelSnapshot {
        private final int fuelLogCount;
        private final Double totalFuelSpend;
        private final Double averageFuelSpend;
        private final Double averageFuelVolume;
        private final Double latestFuelCost;
        private final Double latestFuelVolume;
        private final Integer latestFuelMileage;

        private FuelSnapshot(int fuelLogCount,
                             Double totalFuelSpend,
                             Double averageFuelSpend,
                             Double averageFuelVolume,
                             Double latestFuelCost,
                             Double latestFuelVolume,
                             Integer latestFuelMileage) {
            this.fuelLogCount = fuelLogCount;
            this.totalFuelSpend = totalFuelSpend;
            this.averageFuelSpend = averageFuelSpend;
            this.averageFuelVolume = averageFuelVolume;
            this.latestFuelCost = latestFuelCost;
            this.latestFuelVolume = latestFuelVolume;
            this.latestFuelMileage = latestFuelMileage;
        }
    }
}