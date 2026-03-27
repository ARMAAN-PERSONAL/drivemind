package com.drivemind.service;

import com.drivemind.model.Car;
import com.drivemind.model.FuelLog;
import com.drivemind.repository.CarRepository;
import com.drivemind.repository.FuelLogRepository;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FuelService {

    private final FuelLogRepository fuelLogRepository;
    private final CarRepository carRepository;

    public FuelService(FuelLogRepository fuelLogRepository,
                       CarRepository carRepository) {
        this.fuelLogRepository = fuelLogRepository;
        this.carRepository = carRepository;
    }

    // 🔥 ADD FUEL LOG (SMART)
    public FuelLog addFuelLog(Long carId, FuelLog log) {

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        // 🔥 attach car
        log.setCar(car);

        // 🔥 basic validation
        if (log.getLiters() == null || log.getLiters() <= 0)
            throw new RuntimeException("Invalid fuel amount");

        if (log.getMileage() == null)
            throw new RuntimeException("Mileage required");

        // 🔥 update car mileage
        car.setCurrentMileage(log.getMileage());

        return fuelLogRepository.save(log);
    }

    // 🔥 GET LOGS (SORTED + CALCULATED)
    public List<FuelLog> getFuelLogsForCar(Long carId) {

        List<FuelLog> logs = fuelLogRepository.findByCarId(carId);

        // 🔥 sort by mileage ascending (important for calc)
        logs.sort(Comparator.comparing(FuelLog::getMileage));

        return logs;
    }

    // 🔥 CALCULATE MILEAGE (km/L)
    public double calculateMileage(FuelLog current, FuelLog previous) {

        if (previous == null) return 0;

        int distance = current.getMileage() - previous.getMileage();

        if (distance <= 0 || current.getLiters() == null) return 0;

        return (double) distance / current.getLiters();
    }
}