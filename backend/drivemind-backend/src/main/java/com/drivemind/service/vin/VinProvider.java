package com.drivemind.service.vin;

import com.drivemind.dto.VinResponse;

public interface VinProvider {

    VinResponse decodeVin(String vin);

}