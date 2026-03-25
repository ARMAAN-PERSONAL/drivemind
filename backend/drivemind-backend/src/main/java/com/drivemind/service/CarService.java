package com.drivemind.service;

import com.drivemind.model.Car;
import com.drivemind.model.User;
import com.drivemind.repository.CarRepository;
import com.drivemind.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    public CarService(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    // 🔥 ADD CAR (attach user)
    public Car addCar(Car car, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        car.setUser(user);

        return carRepository.save(car);
    }

    // 🔥 GET ONLY USER'S CARS
    public List<Car> getCarsByUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return carRepository.findByUser(user);
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