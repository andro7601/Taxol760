package com.taxol760.api.drivers.dto;

import com.taxol760.database.model.driver.DriverModel;
import com.taxol760.database.model.driver.DriverStatus;

public record DriverResponse(
        Long id,
        Long userId,
        String licenseNumber,
        DriverStatus status
) {
    public static DriverResponse from(DriverModel driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getUser().getId(),
                driver.getLicenseNumber(),
                driver.getStatus()
        );
    }
}
