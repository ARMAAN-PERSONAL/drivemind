package com.drivemind.controller;

import com.drivemind.dto.AiInsightResponse;
import com.drivemind.service.AiInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final AiInsightService aiInsightService;

    public InsightController(AiInsightService aiInsightService) {
        this.aiInsightService = aiInsightService;
    }

    @GetMapping("/{carId}/ai-summary")
    public ResponseEntity<AiInsightResponse> getAiSummary(@PathVariable Long carId,
                                                          Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(aiInsightService.generateCarInsight(carId, email));
    }
}