package com.drivemind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiInsightResponse {
    private String summary;
    private String recommendation;
    private String overallStatus;   // HEALTHY / DUE_SOON / OVERDUE
    private Integer healthScore;    // out of 100
    private boolean aiGenerated;    // true if OpenAI succeeded, false if fallback
}