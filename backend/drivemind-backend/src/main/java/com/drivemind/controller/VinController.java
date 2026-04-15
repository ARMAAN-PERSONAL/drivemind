package com.drivemind.controller;

import com.drivemind.dto.EnrichedVinResponse;
import com.drivemind.dto.VinResponse;
import com.drivemind.service.VinEnrichmentService;
import com.drivemind.service.VinService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vin")
@CrossOrigin
public class VinController {

    private final VinService vinService;
    private final VinEnrichmentService vinEnrichmentService;

    public VinController(VinService vinService,
                         VinEnrichmentService vinEnrichmentService) {
        this.vinService = vinService;
        this.vinEnrichmentService = vinEnrichmentService;
    }

    @GetMapping("/{vin}")
    public VinResponse decodeVin(@PathVariable String vin) {
        return vinService.decodeVin(vin);
    }

    @GetMapping("/enriched/{vin}")
    public EnrichedVinResponse decodeEnrichedVin(@PathVariable String vin) {
        return vinEnrichmentService.decodeAndEnrich(vin);
    }
}