package com.drivemind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSpotlightResponse {
    private String summary;
    private List<String> funFacts;
    private String powertrainNote;
    private String imageQuery;
    private String imageSearchUrl;
    private boolean aiGenerated;
}