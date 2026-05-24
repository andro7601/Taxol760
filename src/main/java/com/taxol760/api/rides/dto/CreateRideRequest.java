package com.taxol760.api.rides.dto;

public record CreateRideRequest(
            Long riderId,
            Long driverUserId,
            Double pickupLatitude,
            Double pickupLongitude,
            Double dropoffLatitude,
            Double dropoffLongitude
) {
}
