package com.drivemind.service;

import com.drivemind.dto.DashboardSummaryResponse;
import com.drivemind.model.Car;
import com.drivemind.repository.FuelLogRepository;
import com.drivemind.repository.MaintenanceLogRepository;
import com.drivemind.repository.MaintenanceRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final CarService carService;
    private final FuelLogRepository fuelLogRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceRuleRepository maintenanceRuleRepository;

    public DashboardService(CarService carService,
                            FuelLogRepository fuelLogRepository,
                            MaintenanceLogRepository maintenanceLogRepository,
                            MaintenanceRuleRepository maintenanceRuleRepository) {
        this.carService = carService;
        this.fuelLogRepository = fuelLogRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
        this.maintenanceRuleRepository = maintenanceRuleRepository;
    }

    public DashboardSummaryResponse getDashboardSummary(String email) {

        List<Car> cars = carService.getCarsByUser(email);

        if (cars.isEmpty()) {
            return DashboardSummaryResponse.builder()
                    .vehicleCount(0)
                    .fuelLogCount(0)
                    .maintenanceLogCount(0)
                    .maintenanceRuleCount(0)
                    .totalFuelSpend(0.0)
                    .build();
        }

        List<Long> carIds = cars.stream()
                .map(Car::getId)
                .toList();

        long fuelLogCount = fuelLogRepository.countByCarIdIn(carIds);
        long maintenanceLogCount = maintenanceLogRepository.countByCarIdIn(carIds);
        long maintenanceRuleCount = maintenanceRuleRepository.countByCarIdIn(carIds);

        double totalFuelSpend = fuelLogRepository.findByCarIdIn(carIds).stream()
                .mapToDouble(log -> log.getPrice() != null ? log.getPrice() : 0.0)
                .sum();

        return DashboardSummaryResponse.builder()
                .vehicleCount(cars.size())
                .fuelLogCount((int) fuelLogCount)
                .maintenanceLogCount((int) maintenanceLogCount)
                .maintenanceRuleCount((int) maintenanceRuleCount)
                .totalFuelSpend(totalFuelSpend)
                .build();
    }
}