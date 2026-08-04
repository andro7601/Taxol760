package com.taxol760.api.rides;

import com.taxol760.api.rides.dto.CreateRideRequest;
import com.taxol760.api.rides.dto.RideResponse;
import com.taxol760.service.auth.CurrentUserService;
import com.taxol760.service.ride.RideService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RideResponse requestRide(@RequestBody CreateRideRequest request) {
        Long riderId = currentUserService.getCurrentUserId();
        return RideResponse.from(rideService.requestRide(
                request.driverId(),
                request.pickupLatitude(),
                request.pickupLongitude(),
                request.dropoffLatitude(),
                request.dropoffLongitude()
        ));
    }

    @GetMapping("/{id}")
    public RideResponse getRide(@PathVariable Long id) {
        return RideResponse.from(rideService.getRide(id));
    }

    @GetMapping("/me")
    public List<RideResponse> getMyRidesAsRider() {
        Long userId = currentUserService.getCurrentUserId();
        return rideService.getRidesByRiderId(userId).stream()
                .map(RideResponse::from)
                .toList();
    }

    @GetMapping("/me/driver")
    public List<RideResponse> getMyRidesAsDriver() {
        Long userId = currentUserService.getCurrentUserId();
        Long driverId = rideService.getDriverIdByUserId(userId);
        return rideService.getRidesByDriverId(driverId).stream()
                .map(RideResponse::from)
                .toList();
    }

    @PostMapping("/{id}/accept")
    public RideResponse acceptRide(@PathVariable Long id) {
        return RideResponse.from(rideService.acceptRide(id));
    }

    @PostMapping("/{id}/start")
    public RideResponse startRide(@PathVariable Long id) {
        return RideResponse.from(rideService.startRide(id));
    }

    @PostMapping("/{id}/complete")
    public RideResponse completeRide(@PathVariable Long id) {
        return RideResponse.from(rideService.completeRide(id));
    }

    @PostMapping("/{id}/cancel")
    public RideResponse cancelRide(@PathVariable Long id) {
        return RideResponse.from(rideService.cancelRide(id));
    }
}
