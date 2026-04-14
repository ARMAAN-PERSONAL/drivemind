package com.drivemind.service;

import com.drivemind.model.Car;
import com.drivemind.model.MaintenanceRule;
import com.drivemind.repository.CarRepository;
import com.drivemind.repository.MaintenanceRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceRuleService {

    private final MaintenanceRuleRepository maintenanceRuleRepository;
    private final CarRepository carRepository;

    public MaintenanceRuleService(MaintenanceRuleRepository maintenanceRuleRepository,
                                  CarRepository carRepository) {
        this.maintenanceRuleRepository = maintenanceRuleRepository;
        this.carRepository = carRepository;
    }

    public MaintenanceRule addRule(Long carId, MaintenanceRule rule) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        rule.setCar(car);
        return maintenanceRuleRepository.save(rule);
    }

    public List<MaintenanceRule> getRulesForCar(Long carId) {
        return maintenanceRuleRepository.findByCarIdOrderByServiceTypeAsc(carId);
    }

    public void deleteRule(Long ruleId) {
        maintenanceRuleRepository.deleteById(ruleId);
    }

    public MaintenanceRule getRuleById(Long ruleId) {
        return maintenanceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));
    }
}