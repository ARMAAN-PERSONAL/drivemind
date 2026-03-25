package com.drivemind.controller;

import com.drivemind.model.Car;
import com.drivemind.service.CarService;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    // 🔥 ADD CAR (with user)
    @PostMapping
    public Car addCar(@RequestBody Car car, Principal principal) {
        return carService.addCar(car, principal.getName());
    }

    // 🔥 UPDATE MILEAGE (keep ownership intact)
    @PutMapping("/{id}/mileage")
    public Car updateMileage(@PathVariable Long id, @RequestBody Integer mileage, Principal principal) {

        Car car = carService.getCarById(id).orElseThrow();

        // optional: ownership check (good practice)
        if (!car.getUser().getEmail().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        car.setCurrentMileage(mileage);

        return carService.addCar(car, principal.getName());
    }

    // 🔥 GET ONLY USER CARS
    @GetMapping
    public List<Car> getCars(Principal principal) {
        return carService.getCarsByUser(principal.getName());
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id, Principal principal) {

        Car car = carService.getCarById(id).orElseThrow();

        if (!car.getUser().getEmail().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        return car;
    }

    @DeleteMapping("/{id}")
    public void deleteCar(@PathVariable Long id, Principal principal) {

        Car car = carService.getCarById(id).orElseThrow();

        if (!car.getUser().getEmail().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized");
        }

        carService.deleteCar(id);
    }
}