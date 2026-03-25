package com.drivemind.dto;

import lombok.Data;

@Data
public class VinResponse {

    // 🔥 CORE VEHICLE INFO
    private String make;
    private String model;
    private Integer year;
    private String trim;

    private String color;

    private String drivetrain;
    private String vehicleType;
    private String bodyType;

    // 🔥 ENGINE RAW DATA
    private String engineConfiguration;
    private Integer engineCylinders;
    private Double engineDisplacement;
    private Integer enginePower;

    // 🔥 FUEL / TRANS
    private String fuelType;
    private String transmission;

    // 🔥 FINAL SMART OUTPUT
    private String engineDescription;

    // 🔥 NEW: DATA QUALITY FLAGS
    private boolean hasCoreInfo;        // make + model + year
    private boolean hasEngineInfo;      // engine data available

    // 🔥 NEW: SOURCE INFO
    private String dataSource;          // "NHTSA"
    private String status;              // "SUCCESS" | "PARTIAL" | "FAILED"

    // 🔥 NEW: OPTIONAL MESSAGE
    private String message;
}