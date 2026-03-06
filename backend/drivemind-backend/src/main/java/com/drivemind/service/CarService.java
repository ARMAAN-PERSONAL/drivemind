package com.drivemind.service;

import com.drivemind.model.Car;
import com.drivemind.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car addCar(Car car) {
        return carRepository.save(car);
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }

    public Optional<Car> getCarByVin(String vin) {
        return carRepository.findByVin(vin);
    }

    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }
}