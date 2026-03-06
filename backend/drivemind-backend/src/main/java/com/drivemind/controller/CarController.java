package com.drivemind.controller;

import com.drivemind.model.Car;
import com.drivemind.service.CarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping
    public Car addCar(@RequestBody Car car) {
        return carService.addCar(car);
    }

    @PutMapping("/{id}/mileage")
    public Car updateMileage(@PathVariable Long id, @RequestBody Integer mileage) {

        Car car = carService.getCarById(id).orElseThrow();

        car.setCurrentMileage(mileage);

        return carService.addCar(car);
    }

    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carService.getCarById(id).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
    }
}