package com.taxol760.service.ride;

import com.taxol760.store.cache.CacheService;
import com.taxol760.store.model.driver.DriverModel;
import com.taxol760.store.model.ride.RideModel;
import com.taxol760.store.model.ride.RideStatus;
import com.taxol760.store.model.user.UserModel;
import com.taxol760.store.repository.DriverRepository;
import com.taxol760.store.repository.RideRepository;
import com.taxol760.store.repository.UserRepository;
import com.taxol760.store.repository.VehicleRepository;
import com.taxol760.service.auth.CurrentUserService;
import com.taxol760.service.WebSocket.WebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceAccessTest {

    @Mock private RideRepository rideRepository;
    @Mock private UserRepository userRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private WebSocketHandler handler;
    @Mock private CurrentUserService currentUserService;
    @Mock private CacheService cacheService;

    @InjectMocks
    private RideService rideService;

    private RideModel ride;
    private UserModel rider;
    private UserModel stranger;

    @BeforeEach
    void setUp() {
        rider = new UserModel();
        rider.setId(1L);

        DriverModel driver = new DriverModel();
        driver.setId(10L);
        driver.setUser(rider); // driver is same user as rider for simplicity

        ride = new RideModel();
        ride.setId(1L);
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setStatus(RideStatus.REQUESTED);

        stranger = new UserModel();
        stranger.setId(999L); // completely unrelated user
    }

    @Test
    void getRide_throwsAccessDenied_whenUserIsNeitherRiderNorDriver() {
        // A stranger (id=999) tries to fetch a ride that belongs to rider (id=1)
        when(currentUserService.getCurrentUserId()).thenReturn(999L);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(AccessDeniedException.class, () -> rideService.getRide(1L));
    }

    @Test
    void getRide_succeeds_whenCurrentUserIsRider() {
        // The actual rider (id=1) fetches their own ride — should work fine
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        RideModel result = rideService.getRide(1L);

        org.junit.jupiter.api.Assertions.assertEquals(RideStatus.REQUESTED, result.getStatus());
    }
}
