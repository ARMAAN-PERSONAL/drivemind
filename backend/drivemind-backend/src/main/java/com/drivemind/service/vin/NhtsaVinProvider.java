package com.drivemind.service.vin;

import com.drivemind.dto.VinResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class NhtsaVinProvider implements VinProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public VinResponse decodeVin(String vin) {

        VinResponse vinResponse = new VinResponse();

        try {

            String url = "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValuesExtended/" + vin + "?format=json";

            Map response = restTemplate.getForObject(url, Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("Results");

            if (results == null || results.isEmpty()) {
                return buildFailure("No VIN data found");
            }

            Map<String, Object> result = results.get(0);

            // 🔥 BASIC INFO
            vinResponse.setMake(clean(getString(result, "Make")));
            vinResponse.setModel(clean(getString(result, "Model")));
            vinResponse.setTrim(clean(getString(result, "Trim")));

            String year = clean(getString(result, "ModelYear"));
            if (year != null) vinResponse.setYear(parseInt(year));

            vinResponse.setBodyType(clean(getString(result, "BodyClass")));
            vinResponse.setVehicleType(clean(getString(result, "VehicleType")));
            vinResponse.setDrivetrain(clean(getString(result, "DriveType")));

            vinResponse.setFuelType(clean(getString(result, "FuelTypePrimary")));
            vinResponse.setTransmission(clean(getString(result, "TransmissionStyle")));

            vinResponse.setEngineConfiguration(clean(getString(result, "EngineConfiguration")));

            String cylinders = clean(getString(result, "EngineCylinders"));
            if (cylinders != null) vinResponse.setEngineCylinders(parseInt(cylinders));

            String displacement = clean(getString(result, "DisplacementL"));
            if (displacement != null) vinResponse.setEngineDisplacement(parseDouble(displacement));

            String hp = clean(getString(result, "EngineHP"));
            if (hp != null) vinResponse.setEnginePower(parseInt(hp));

            vinResponse.setColor(clean(getString(result, "ExteriorColor")));

            // 🔥 ENGINE DESCRIPTION
            vinResponse.setEngineDescription(buildEngineDescription(vinResponse));

            // 🔥 FLAGS
            vinResponse.setHasCoreInfo(
                    vinResponse.getMake() != null &&
                            vinResponse.getModel() != null &&
                            vinResponse.getYear() != null
            );

            vinResponse.setHasEngineInfo(
                    vinResponse.getEngineDescription() != null &&
                            !vinResponse.getEngineDescription().isBlank()
            );

            // 🔥 STATUS LOGIC
            if (vinResponse.isHasCoreInfo()) {
                vinResponse.setStatus("SUCCESS");
            } else {
                vinResponse.setStatus("PARTIAL");
                vinResponse.setMessage("Some vehicle details could not be decoded");
            }

            vinResponse.setDataSource("NHTSA");

            return vinResponse;

        } catch (Exception e) {
            return buildFailure("VIN decode failed");
        }
    }

    // 🔥 CLEAN BAD VALUES
    private String clean(String value) {
        if (value == null ||
                value.equals("0") ||
                value.equalsIgnoreCase("Not Applicable") ||
                value.isBlank()) {
            return null;
        }
        return value;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private Integer parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (Exception e) { return null; }
    }

    private Double parseDouble(String value) {
        try { return Double.parseDouble(value); }
        catch (Exception e) { return null; }
    }

    // 🔥 SMART ENGINE DESCRIPTION
    private String buildEngineDescription(VinResponse vin) {

        StringBuilder engine = new StringBuilder();

        if (vin.getEngineDisplacement() != null) {
            engine.append(vin.getEngineDisplacement()).append("L ");
        }

        if (vin.getEngineConfiguration() != null) {
            engine.append(vin.getEngineConfiguration()).append(" ");
        }

        if (vin.getEngineCylinders() != null) {
            engine.append(vin.getEngineCylinders()).append(" Cyl ");
        }

        if (vin.getEnginePower() != null) {
            engine.append("(").append(vin.getEnginePower()).append(" HP)");
        }

        String result = engine.toString().trim();

        return result.isBlank() ? "Engine info not available" : result;
    }

    // 🔥 FAILURE BUILDER
    private VinResponse buildFailure(String message) {
        VinResponse res = new VinResponse();
        res.setStatus("FAILED");
        res.setMessage(message);
        res.setDataSource("NHTSA");
        return res;
    }
}