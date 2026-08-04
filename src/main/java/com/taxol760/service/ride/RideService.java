package com.taxol760.service.ride;

import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.ride.RideModel;
import com.taxol760.databaseANDcache.model.ride.RideStatus;
import com.taxol760.databaseANDcache.model.user.UserModel;
import com.taxol760.databaseANDcache.model.vehicle.VehicleModel;
import com.taxol760.databaseANDcache.repository.DriverRepository;
import com.taxol760.databaseANDcache.repository.RideRepository;
import com.taxol760.databaseANDcache.repository.UserRepository;
import com.taxol760.databaseANDcache.repository.VehicleRepository;
import com.taxol760.databaseANDcache.cache.cacheservice;
import com.taxol760.service.auth.CurrentUserService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import com.taxol760.service.WebSocket.WebSocketHandler;

@Service
@RequiredArgsConstructor
@Transactional
public class RideService {
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final WebSocketHandler handler;
    private final CurrentUserService currentUserService;
    private final cacheservice cacheservice;

    @Transactional(readOnly = true)
    public Long getDriverIdByUserId(Long userId) {
        return driverRepository.findByUser_Id(userId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found for user"))
                .getId();
    }

    public RideModel requestRide(
            Long driverId,
            Double pickupLatitude,
            Double pickupLongitude,
            Double dropoffLatitude,
            Double dropoffLongitude
    ) {
        long riderId=currentUserService.getCurrentUserId();
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
        DriverModel driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));
        ride.setDriver(driver);

        VehicleModel vehicle = vehicleRepository.findByDriver(driver)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
        ride.setVehicle(vehicle);
        RideModel savedRide = rideRepository.save(ride);
        
        Long driverUserId = driver.getUser().getId();
        handler.notifyDriverOfRide(driverUserId, savedRide);
        return savedRide;
    }

    public RideModel acceptRide(Long rideId) {
        Long userId = currentUserService.getCurrentUserId();
        Long driverId = getDriverIdByUserId(userId);
        
        RideModel ride = getRide(rideId);
        requireStatus(ride, RideStatus.REQUESTED);
        ensureCurrentUserIsAssignedDriver(ride);

        DriverModel driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));
        ensureDriverIsNotRider(ride, driver);

        VehicleModel vehicle = vehicleRepository.findByDriver(driver)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));

        ride.setDriver(driver);
        ride.setVehicle(vehicle);
        ride.setStatus(RideStatus.ACCEPTED);
        
        cacheservice.setDriverOccupied(driverId.intValue());

        return rideRepository.save(ride);
    }

    public RideModel startRide(Long rideId) {
        RideModel ride = getRide(rideId);
        requireStatus(ride, RideStatus.ACCEPTED);
        ensureCurrentUserIsAssignedDriver(ride);

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(LocalDateTime.now());

        return rideRepository.save(ride);
    }

    public RideModel completeRide(Long rideId) {
        RideModel ride = getRide(rideId);
        requireStatus(ride, RideStatus.IN_PROGRESS);
        ensureCurrentUserIsAssignedDriver(ride);

        ride.setStatus(RideStatus.COMPLETED);
        ride.setFinishedAt(LocalDateTime.now());
        
        cacheservice.setDriverFree(ride.getDriver().getId().intValue());

        return rideRepository.save(ride);
    }

    public RideModel cancelRide(Long rideId) {
        RideModel ride = getRide(rideId);
        ensureCurrentUserCanAccessRide(ride);
        
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalStateException("Completed rides cannot be cancelled");
        }

        ride.setStatus(RideStatus.CANCELLED);
        handler.notifyRiderOfReject(ride.getDriver().getUser().getId().intValue(),ride);
        
        cacheservice.setDriverFree(ride.getDriver().getId().intValue());
        
        return rideRepository.save(ride);
    }

    @Transactional(readOnly = true)
    public RideModel getRide(Long id) {
        RideModel ride = rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found"));
        ensureCurrentUserCanAccessRide(ride);
        return ride;
    }

    @Transactional(readOnly = true)
    public List<RideModel> getRidesByRider(UserModel rider) {
        return rideRepository.findByRider(rider);
    }

    @Transactional(readOnly = true)
    public List<RideModel> getRidesByRiderId(Long riderId) {
        UserModel rider = userRepository.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found"));

        return getRidesByRider(rider);
    }

    @Transactional(readOnly = true)
    public List<RideModel> getRidesByDriver(DriverModel driver) {
        return rideRepository.findByDriver(driver);
    }

    @Transactional(readOnly = true)
    public List<RideModel> getRidesByDriverId(Long driverId) {
        DriverModel driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));

        return getRidesByDriver(driver);
    }

    @Transactional(readOnly = true)
    public RideModel getActiveRideForDriver(Long driverId) {
        DriverModel driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null) return null;
        
        List<RideModel> accepted = rideRepository.findByDriverAndStatus(driver, RideStatus.ACCEPTED);
        if (!accepted.isEmpty()) return accepted.get(0);
        
        List<RideModel> inProgress = rideRepository.findByDriverAndStatus(driver, RideStatus.IN_PROGRESS);
        if (!inProgress.isEmpty()) return inProgress.get(0);
        
        return null;
    }

    @Transactional(readOnly = true)
    public List<RideModel> getRidesByStatus(RideStatus status) {
        return rideRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
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
            throw new IllegalStateException("Driver cannot accept their own ride request");
        }
    }

    private void ensureCurrentUserIsAssignedDriver(RideModel ride) {
        Long currentUserId = currentUserService.getCurrentUserId();
        if (ride.getDriver() == null || !ride.getDriver().getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not the assigned driver for this ride");
        }
    }

    private void ensureCurrentUserCanAccessRide(RideModel ride) {
        Long currentUserId = currentUserService.getCurrentUserId();
        boolean isRider = ride.getRider().getId().equals(currentUserId);
        boolean isDriver = ride.getDriver() != null && ride.getDriver().getUser().getId().equals(currentUserId);
        
        if (!isRider && !isDriver) {
            throw new AccessDeniedException("You do not have access to this ride");
        }
    }
}
