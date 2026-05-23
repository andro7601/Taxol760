package com.taxol760.databaseANDcache.cache;

import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.driver.DriverStatus;
import com.taxol760.databaseANDcache.model.vehicle.VehicleModel;

public record CachedDriverInfo(
        Long id,
        Long userId,
        String licenseNumber,
        DriverStatus status,
        String vehicleBrand,
        String vehicleModel,
        String vehicleColor,
        String vehiclePlateNumber
) {
    public static CachedDriverInfo from(DriverModel driver, VehicleModel vehicle) {
        return new CachedDriverInfo(
                driver.getId(),
                driver.getUser().getId(),
                driver.getLicenseNumber(),
                driver.getStatus(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getPlateNumber()
        );
    }
}
