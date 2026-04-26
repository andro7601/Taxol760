package com.taxol760.api.rides.dto;

public record CreateRideRequest(
        Long riderId,
        Double pickupLatitude,
        Double pickupLongitude,
        Double dropoffLatitude,
        Double dropoffLongitude
) {
}
