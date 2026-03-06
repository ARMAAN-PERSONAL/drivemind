package com.drivemind.controller;

import com.drivemind.model.MaintenanceLog;
import com.drivemind.service.MaintenanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    public MaintenanceLog addMaintenance(@RequestBody MaintenanceLog log) {
        return maintenanceService.addMaintenanceLog(log);
    }

    @GetMapping("/{carId}")
    public List<MaintenanceLog> getLogs(@PathVariable Long carId) {
        return maintenanceService.getLogsForCar(carId);
    }
}