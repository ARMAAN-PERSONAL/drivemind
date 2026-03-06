package com.drivemind.service;

import com.drivemind.model.MaintenanceLog;
import com.drivemind.repository.MaintenanceLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceService {

    private final MaintenanceLogRepository maintenanceLogRepository;

    public MaintenanceService(MaintenanceLogRepository maintenanceLogRepository) {
        this.maintenanceLogRepository = maintenanceLogRepository;
    }

    public MaintenanceLog addMaintenanceLog(MaintenanceLog log) {
        return maintenanceLogRepository.save(log);
    }

    public List<MaintenanceLog> getLogsForCar(Long carId) {
        return maintenanceLogRepository.findByCarId(carId);
    }
}