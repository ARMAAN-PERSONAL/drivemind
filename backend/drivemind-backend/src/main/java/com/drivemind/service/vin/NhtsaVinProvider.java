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

        String url = "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValuesExtended/" + vin + "?format=json";

        Map response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("Results");

        Map<String, Object> result = results.get(0);

        VinResponse vinResponse = new VinResponse();

        vinResponse.setMake(getString(result, "Make"));
        vinResponse.setModel(getString(result, "Model"));
        vinResponse.setTrim(getString(result, "Trim"));

        String year = getString(result, "ModelYear");
        if (year != null) vinResponse.setYear(Integer.parseInt(year));

        vinResponse.setBodyType(getString(result, "BodyClass"));
        vinResponse.setVehicleType(getString(result, "VehicleType"));
        vinResponse.setDrivetrain(getString(result, "DriveType"));

        vinResponse.setFuelType(getString(result, "FuelTypePrimary"));
        vinResponse.setTransmission(getString(result, "TransmissionStyle"));

        vinResponse.setEngineConfiguration(getString(result, "EngineConfiguration"));

        String cylinders = getString(result, "EngineCylinders");
        if (cylinders != null) {
            vinResponse.setEngineCylinders(Integer.parseInt(cylinders));
        }

        String displacement = getString(result, "DisplacementL");
        if (displacement != null) {
            vinResponse.setEngineDisplacement(Double.parseDouble(displacement));
        }

        String hp = getString(result, "EngineHP");
        if (hp != null) {
            vinResponse.setEnginePower(Integer.parseInt(hp));
        }

        vinResponse.setColor(getString(result, "ExteriorColor"));

        // Fallback engine description
        vinResponse.setEngineDescription(buildEngineDescription(vinResponse));

        return vinResponse;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value == null) return null;

        String str = value.toString();

        if (str.isBlank() || str.equalsIgnoreCase("null")) return null;

        return str;
    }

    private String buildEngineDescription(VinResponse vin) {

        StringBuilder engine = new StringBuilder();

        if (vin.getEngineDisplacement() != null) {
            engine.append(vin.getEngineDisplacement()).append("L ");
        }

        if (vin.getEngineConfiguration() != null) {
            engine.append(vin.getEngineConfiguration()).append(" ");
        }

        if (vin.getEngineCylinders() != null) {
            engine.append(vin.getEngineCylinders()).append(" Cylinder");
        }

        return engine.toString().trim();
    }
}