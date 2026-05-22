package com.taxol760.api.drivers.dto;

import com.taxol760.databaseANDcache.cache.CachedDriverInfo;
import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.driver.DriverStatus;

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

    public static DriverResponse from(CachedDriverInfo driver) {
        return new DriverResponse(
                driver.id(),
                driver.userId(),
                driver.licenseNumber(),
                driver.status()
        );
    }
}
