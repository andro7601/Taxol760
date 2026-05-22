package com.taxol760.databaseANDcache.cache;

import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.driver.DriverStatus;

public record CachedDriverInfo(
        Long id,
        Long userId,
        String licenseNumber,
        DriverStatus status
) {
    public static CachedDriverInfo from(DriverModel driver) {
        return new CachedDriverInfo(
                driver.getId(),
                driver.getUser().getId(),
                driver.getLicenseNumber(),
                driver.getStatus()
        );
    }
}
