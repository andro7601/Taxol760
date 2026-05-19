package com.taxol760.service.ride;

import com.taxol760.database.model.driver.DriverModel;
import com.taxol760.database.model.ride.RideModel;
import com.taxol760.database.model.ride.RideStatus;
import com.taxol760.database.model.user.UserModel;
import com.taxol760.database.model.vehicle.VehicleModel;
import com.taxol760.database.repository.DriverRepository;
import com.taxol760.database.repository.RideRepository;
import com.taxol760.database.repository.UserRepository;
import com.taxol760.database.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RideService {
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.isCurrentUser(#riderId)")
    public RideModel requestRide(
            Long riderId,
            Double pickupLatitude,
            Double pickupLongitude,
            Double dropoffLatitude,
            Double dropoffLongitude
    ) {
        UserModel rider = userRepository.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found"));

        RideModel ride = new RideModel();
        ride.setRider(rider);
        ride.setPickupLatitude(pickupLatitude);
        ride.setPickupLongitude(pickupLongitude);
        ride.setDropoffLatitude(dropoffLatitude);
        ride.setDropoffLongitude(dropoffLongitude);
        ride.setStatus(RideStatus.REQUESTED);
        ride.setCreatedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.isCurrentDriver(#driverId)")
    public RideModel acceptRide(Long rideId, Long driverId) {
        RideModel ride = getRide(rideId);
        requireStatus(ride, RideStatus.REQUESTED);

        DriverModel driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));
        ensureDriverIsNotRider(ride, driver);

        VehicleModel vehicle = vehicleRepository.findByDriver(driver)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        ride.setDriver(driver);
        ride.setVehicle(vehicle);
        ride.setStatus(RideStatus.ACCEPTED);

        return rideRepository.save(ride);
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.isAssignedDriverForRide(#rideId)")
    public RideModel startRide(Long rideId) {
        RideModel ride = getRide(rideId);
        requireStatus(ride, RideStatus.ACCEPTED);

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.isAssignedDriverForRide(#rideId)")
    public RideModel completeRide(Long rideId) {
        RideModel ride = getRide(rideId);
        requireStatus(ride, RideStatus.IN_PROGRESS);

        ride.setStatus(RideStatus.COMPLETED);
        ride.setFinishedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.canAccessRide(#rideId)")
    public RideModel cancelRide(Long rideId) {
        RideModel ride = getRide(rideId);
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalStateException("Completed rides cannot be cancelled");
        }

        ride.setStatus(RideStatus.CANCELLED);

        return rideRepository.save(ride);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.canAccessRide(#id)")
    public RideModel getRide(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found"));
    }

    @Transactional(readOnly = true)
    public List<RideModel> getRidesByRider(UserModel rider) {
        return rideRepository.findByRider(rider);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.isCurrentUser(#riderId)")
    public List<RideModel> getRidesByRiderId(Long riderId) {
        UserModel rider = userRepository.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found"));

        return getRidesByRider(rider);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<RideModel> getRidesByDriver(DriverModel driver) {
        return rideRepository.findByDriver(driver);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.isCurrentDriver(#driverId)")
    public List<RideModel> getRidesByDriverId(Long driverId) {
        DriverModel driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));

        return getRidesByDriver(driver);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<RideModel> getRidesByStatus(RideStatus status) {
        return rideRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<RideModel> getAllRides() {
        return rideRepository.findAll();
    }

    private void requireStatus(RideModel ride, RideStatus expectedStatus) {
        if (ride.getStatus() != expectedStatus) {
            throw new IllegalStateException("Ride must be " + expectedStatus);
        }
    }

    private void ensureDriverIsNotRider(RideModel ride, DriverModel driver) {
        if (ride.getRider().getId().equals(driver.getUser().getId())) {
            throw new AccessDeniedException("Driver cannot accept their own ride request");
        }
    }
}
