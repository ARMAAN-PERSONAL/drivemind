package com.drivemind.controller;

import com.drivemind.model.Car;
import com.drivemind.service.CarService;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    private final CarService carService;

    public DashboardController(CarService carService) {
        this.carService = carService;
    }

    // 🔥 USER-SPECIFIC DASHBOARD CARS
    @GetMapping("/cars")
    public List<Car> getDashboardCars(Principal principal) {
        return carService.getCarsByUser(principal.getName());
    }
}