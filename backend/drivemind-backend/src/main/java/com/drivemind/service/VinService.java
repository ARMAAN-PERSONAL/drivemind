package com.drivemind.service;

import com.drivemind.dto.VinResponse;
import com.drivemind.service.vin.VinProvider;
import org.springframework.stereotype.Service;

@Service
public class VinService {

    private final VinProvider vinProvider;

    public VinService(VinProvider vinProvider) {
        this.vinProvider = vinProvider;
    }

    public VinResponse decodeVin(String vin) {
        return vinProvider.decodeVin(vin);
    }
}