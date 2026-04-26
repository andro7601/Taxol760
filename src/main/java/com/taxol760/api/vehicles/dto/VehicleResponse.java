package com.taxol760.api.vehicles.dto;

import com.taxol760.database.model.vehicle.VehicleModel;

public record VehicleResponse(
        Long id,
        Long driverId,
        String brand,
        String model,
        String color,
        String plateNumber
) {
    public static VehicleResponse from(VehicleModel vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getDriver().getId(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getColor(),
                vehicle.getPlateNumber()
        );
    }
}
