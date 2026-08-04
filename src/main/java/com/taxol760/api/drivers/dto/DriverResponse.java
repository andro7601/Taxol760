package com.taxol760.api.drivers.dto;

import com.taxol760.databaseANDcache.cache.CachedDriverInfo;
import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.driver.DriverStatus;
import com.taxol760.databaseANDcache.model.vehicle.VehicleModel;

public record DriverResponse(
        Long id,
        Long userId,
        String licenseNumber,
        DriverStatus status,
        String vehicleBrand,
        String vehicleModel,
        String vehicleColor,
        String vehiclePlateNumber
) {
    public static DriverResponse from(DriverModel driver) {
        VehicleModel vehicle = driver.getVehicle();
        return new DriverResponse(
                driver.getId(),
                driver.getUser().getId(),
                driver.getLicenseNumber(),
                driver.getStatus(),
                vehicle != null ? vehicle.getBrand() : null,
                vehicle != null ? vehicle.getModel() : null,
                vehicle != null ? vehicle.getColor() : null,
                vehicle != null ? vehicle.getPlateNumber() : null
        );
    }

    public static DriverResponse from(DriverModel driver, VehicleModel vehicle) {
        return new DriverResponse(
                driver.getId(),
                driver.getUser().getId(),
                driver.getLicenseNumber(),
                driver.getStatus(),
                vehicle != null ? vehicle.getBrand() : null,
                vehicle != null ? vehicle.getModel() : null,
                vehicle != null ? vehicle.getColor() : null,
                vehicle != null ? vehicle.getPlateNumber() : null
        );
    }

    public static DriverResponse from(CachedDriverInfo driver) {
        return new DriverResponse(
                driver.id(),
                driver.userId(),
                driver.licenseNumber(),
                driver.status(),
                driver.vehicleBrand(),
                driver.vehicleModel(),
                driver.vehicleColor(),
                driver.vehiclePlateNumber()
        );
    }
}
