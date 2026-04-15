package com.drivemind.dto;

import lombok.Data;

@Data
public class GeneratedRuleRequest {
    private String serviceType;
    private Integer intervalKm;
    private String description;
}