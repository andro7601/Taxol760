package com.taxol760.api.vehicles.dto;

public record CreateVehicleRequest(
        Long driverId,
        String brand,
        String model,
        String color,
        String plateNumber
) {
}
