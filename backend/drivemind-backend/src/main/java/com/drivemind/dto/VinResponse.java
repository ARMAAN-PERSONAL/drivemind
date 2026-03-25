package com.drivemind.dto;

import lombok.Data;

@Data
public class VinResponse {

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
}