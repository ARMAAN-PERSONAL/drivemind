package com.drivemind.service;

import com.drivemind.model.FuelLog;
import com.drivemind.repository.FuelLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FuelService {

    private final FuelLogRepository fuelLogRepository;

    public FuelService(FuelLogRepository fuelLogRepository) {
        this.fuelLogRepository = fuelLogRepository;
    }

    public FuelLog addFuelLog(FuelLog log) {
        return fuelLogRepository.save(log);
    }

    public List<FuelLog> getFuelLogsForCar(Long carId) {
        return fuelLogRepository.findByCarId(carId);
    }
}