package com.drivemind.controller;

import com.drivemind.model.FuelLog;
import com.drivemind.service.FuelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel")
@CrossOrigin
public class FuelController {

    private final FuelService fuelService;

    public FuelController(FuelService fuelService) {
        this.fuelService = fuelService;
    }

    @PostMapping
    public FuelLog addFuelLog(@RequestBody FuelLog log) {
        return fuelService.addFuelLog(log);
    }

    @GetMapping("/{carId}")
    public List<FuelLog> getFuelLogs(@PathVariable Long carId) {
        return fuelService.getFuelLogsForCar(carId);
    }
}