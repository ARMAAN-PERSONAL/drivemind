package com.drivemind.controller;

import com.drivemind.model.Car;
import com.drivemind.service.CarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    private final CarService carService;

    public DashboardController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/cars")
    public List<Car> getDashboardCars() {
        return carService.getAllCars();
    }
}