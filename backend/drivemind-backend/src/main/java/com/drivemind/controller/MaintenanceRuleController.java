package com.drivemind.controller;

import com.drivemind.model.Car;
import com.drivemind.model.MaintenanceRule;
import com.drivemind.service.CarService;
import com.drivemind.service.MaintenanceRuleService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance-rules")
@CrossOrigin
public class MaintenanceRuleController {

    private final MaintenanceRuleService maintenanceRuleService;
    private final CarService carService;

    public MaintenanceRuleController(MaintenanceRuleService maintenanceRuleService,
                                     CarService carService) {
        this.maintenanceRuleService = maintenanceRuleService;
        this.carService = carService;
    }

    @PostMapping("/{carId}")
    public MaintenanceRule addRule(@PathVariable Long carId,
                                   @RequestBody MaintenanceRule rule,
                                   Principal principal) {
        Car car = carService.getCarById(carId).orElseThrow();

        if (!car.getUser().getEmail().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        return maintenanceRuleService.addRule(carId, rule);
    }

    @GetMapping("/{carId}")
    public List<MaintenanceRule> getRulesForCar(@PathVariable Long carId,
                                                Principal principal) {
        Car car = carService.getCarById(carId).orElseThrow();

        if (!car.getUser().getEmail().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        return maintenanceRuleService.getRulesForCar(carId);
    }

    @DeleteMapping("/{ruleId}")
    public void deleteRule(@PathVariable Long ruleId, Principal principal) {
        MaintenanceRule rule = maintenanceRuleService.getRuleById(ruleId);

        if (!rule.getCar().getUser().getEmail().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        maintenanceRuleService.deleteRule(ruleId);
    }
}