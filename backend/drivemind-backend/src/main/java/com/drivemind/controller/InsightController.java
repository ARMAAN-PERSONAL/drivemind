package com.drivemind.controller;

import com.drivemind.dto.AiInsightResponse;
import com.drivemind.dto.VehicleSpotlightResponse;
import com.drivemind.service.AiInsightService;
import com.drivemind.service.VehicleSpotlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final AiInsightService aiInsightService;
    private final VehicleSpotlightService vehicleSpotlightService;

    public InsightController(AiInsightService aiInsightService,
                             VehicleSpotlightService vehicleSpotlightService) {
        this.aiInsightService = aiInsightService;
        this.vehicleSpotlightService = vehicleSpotlightService;
    }

    @GetMapping("/{carId}/ai-summary")
    public ResponseEntity<AiInsightResponse> getAiSummary(@PathVariable Long carId,
                                                          Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(aiInsightService.generateCarInsight(carId, email));
    }

    @GetMapping("/{carId}/vehicle-spotlight")
    public ResponseEntity<VehicleSpotlightResponse> getVehicleSpotlight(@PathVariable Long carId,
                                                                        Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(vehicleSpotlightService.generateVehicleSpotlight(carId, email));
    }
}