package com.taxol760.api.rides.dto;

public record CreateRideRequest(
            Long driverId,
            Double pickupLatitude,
            Double pickupLongitude,
            Double dropoffLatitude,
            Double dropoffLongitude
) {}
