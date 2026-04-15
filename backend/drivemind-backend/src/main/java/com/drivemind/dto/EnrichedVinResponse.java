package com.drivemind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedVinResponse {
    private String make;
    private String model;
    private Integer year;
    private String trim;

    private String color;
    private String drivetrain;
    private String vehicleType;
    private String bodyType;

    private String engineConfiguration;
    private Integer engineCylinders;
    private Double engineDisplacement;
    private Integer enginePower;

    private String fuelType;
    private String transmission;
    private String engineDescription;

    private String vehicleCategory;
    private String shortSummary;
    private String confidenceLabel;

    private boolean hasCoreInfo;
    private boolean hasEngineInfo;
    private boolean aiGenerated;

    private String dataSource;
    private String status;
    private String message;
}