package com.taxol760.api.dto;

import com.taxol760.database.models.ride.RideModel;
import com.taxol760.database.models.ride.RideStatus;
import java.time.LocalDateTime;

public record RideResponse(
        Long id,
        Long riderId,
        Long driverId,
        Long vehicleId,
        Double pickupLatitude,
        Double pickupLongitude,
        Double dropoffLatitude,
        Double dropoffLongitude,
        RideStatus status,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt
) {
    public static RideResponse from(RideModel ride) {
        Long driverId = ride.getDriver() == null ? null : ride.getDriver().getId();
        Long vehicleId = ride.getVehicle() == null ? null : ride.getVehicle().getId();

        return new RideResponse(
                ride.getId(),
                ride.getRider().getId(),
                driverId,
                vehicleId,
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                ride.getDropoffLatitude(),
                ride.getDropoffLongitude(),
                ride.getStatus(),
                ride.getCreatedAt(),
                ride.getStartedAt(),
                ride.getFinishedAt()
        );
    }
}
