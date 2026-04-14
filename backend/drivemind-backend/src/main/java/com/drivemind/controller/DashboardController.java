package com.drivemind.controller;

import com.drivemind.dto.DashboardSummaryResponse;
import com.drivemind.model.Car;
import com.drivemind.service.CarService;
import com.drivemind.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    private final CarService carService;
    private final DashboardService dashboardService;

    public DashboardController(CarService carService, DashboardService dashboardService) {
        this.carService = carService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/cars")
    public List<Car> getDashboardCars(Principal principal) {
        return carService.getCarsByUser(principal.getName());
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary(Principal principal) {
        return dashboardService.getDashboardSummary(principal.getName());
    }
}