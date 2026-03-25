package com.drivemind.controller;

import com.drivemind.dto.VinResponse;
import com.drivemind.service.VinService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vin")
@CrossOrigin
public class VinController {

    private final VinService vinService;

    public VinController(VinService vinService) {
        this.vinService = vinService;
    }

    @GetMapping("/{vin}")
    public VinResponse decodeVin(@PathVariable String vin) {

        return vinService.decodeVin(vin);

    }
}