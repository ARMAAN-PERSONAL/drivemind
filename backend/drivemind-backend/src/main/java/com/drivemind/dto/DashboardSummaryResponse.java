package com.drivemind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private int vehicleCount;
    private int fuelLogCount;
    private int maintenanceLogCount;
    private int maintenanceRuleCount;
    private double totalFuelSpend;
}