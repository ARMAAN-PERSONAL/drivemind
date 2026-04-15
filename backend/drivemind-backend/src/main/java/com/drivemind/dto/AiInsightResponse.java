package com.drivemind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInsightResponse {
    private String summary;
    private String recommendation;
    private String overallStatus;
    private Integer healthScore;
    private boolean aiGenerated;

    private Integer fuelLogCount;
    private Double totalFuelSpend;
    private Double averageFuelSpend;
    private Double averageFuelVolume;
    private Double latestFuelCost;
    private Double latestFuelVolume;
    private Integer latestFuelMileage;
}