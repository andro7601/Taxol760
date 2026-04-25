package com.taxol760.api.controller.dto;

public record CreateRideRequest(
        Long riderId,
        Double pickupLatitude,
        Double pickupLongitude,
        Double dropoffLatitude,
        Double dropoffLongitude
) {
}
