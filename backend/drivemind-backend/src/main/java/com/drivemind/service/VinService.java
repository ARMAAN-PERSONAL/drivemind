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

        // 🔥 STEP 1: BASIC VALIDATION
        if (vin == null || vin.trim().length() != 17) {
            return buildFailure("Invalid VIN (must be 17 characters)");
        }

        vin = vin.trim().toUpperCase();

        try {

            VinResponse response = vinProvider.decodeVin(vin);

            // 🔥 STEP 2: SAFETY CHECK
            if (response == null) {
                return buildFailure("VIN decode returned no data");
            }

            // 🔥 STEP 3: FINAL NORMALIZATION
            normalize(response);

            return response;

        } catch (Exception e) {
            return buildFailure("VIN decode service error");
        }
    }

    // 🔥 CLEAN RESPONSE BEFORE SENDING TO FRONTEND
    private void normalize(VinResponse res) {

        if (res.getMake() != null) {
            res.setMake(capitalize(res.getMake()));
        }

        if (res.getModel() != null) {
            res.setModel(res.getModel().trim());
        }

        if (res.getTrim() != null) {
            res.setTrim(res.getTrim().trim());
        }

        // fallback message if partial
        if ("PARTIAL".equals(res.getStatus()) && res.getMessage() == null) {
            res.setMessage("Partial vehicle data available");
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // 🔥 FAILURE RESPONSE BUILDER
    private VinResponse buildFailure(String message) {
        VinResponse res = new VinResponse();
        res.setStatus("FAILED");
        res.setMessage(message);
        res.setDataSource("SYSTEM");
        return res;
    }
}