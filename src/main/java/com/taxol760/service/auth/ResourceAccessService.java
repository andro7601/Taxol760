package com.taxol760.service.auth;

import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.ride.RideModel;
import com.taxol760.databaseANDcache.model.user.UserModel;
import com.taxol760.databaseANDcache.model.vehicle.VehicleModel;
import com.taxol760.databaseANDcache.repository.DriverRepository;
import com.taxol760.databaseANDcache.repository.RideRepository;
import com.taxol760.databaseANDcache.repository.UserRepository;
import com.taxol760.databaseANDcache.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("resourceAccess")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceAccessService {
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final RideRepository rideRepository;

    public boolean isCurrentUser(Long userId) {
        return currentUser()
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    public boolean isCurrentDriver(Long driverId) {
        return currentUser()
                .flatMap(driverRepository::findByUser)
                .map(driver -> driver.getId().equals(driverId))
                .orElse(false);
    }

    public boolean canAccessVehicle(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(this::isCurrentDriverVehicle)
                .orElse(false);
    }

    public boolean canAccessRide(Long rideId) {
        return rideRepository.findById(rideId)
                .map(this::isCurrentUserRide)
                .orElse(false);
    }

    public boolean isAssignedDriverForRide(Long rideId) {
        return rideRepository.findById(rideId)
                .map(ride -> currentDriver()
                        .map(driver -> ride.getDriver() != null && ride.getDriver().getId().equals(driver.getId()))
                        .orElse(false))
                .orElse(false);
    }

    private boolean isCurrentDriverVehicle(VehicleModel vehicle) {
        return currentDriver()
                .map(driver -> vehicle.getDriver().getId().equals(driver.getId()))
                .orElse(false);
    }

    private boolean isCurrentUserRide(RideModel ride) {
        return currentUser()
                .map(user -> isRider(ride, user) || isDriverUser(ride, user))
                .orElse(false);
    }

    private boolean isRider(RideModel ride, UserModel user) {
        return ride.getRider().getId().equals(user.getId());
    }

    private boolean isDriverUser(RideModel ride, UserModel user) {
        return ride.getDriver() != null && ride.getDriver().getUser().getId().equals(user.getId());
    }

    private java.util.Optional<DriverModel> currentDriver() {
        return currentUser().flatMap(driverRepository::findByUser);
    }

    private java.util.Optional<UserModel> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername());
        }

        if (principal instanceof String username && !"anonymousUser".equals(username)) {
            return userRepository.findByEmail(username);
        }

        return java.util.Optional.empty();
    }
}
