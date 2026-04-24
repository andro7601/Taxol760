package com.taxol760.api.controllers;

import com.taxol760.api.dto.CreateRideRequest;
import com.taxol760.api.dto.RideResponse;
import com.taxol760.database.models.ride.RideStatus;
import com.taxol760.services.RideService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RideResponse requestRide(@RequestBody CreateRideRequest request) {
        return RideResponse.from(rideService.requestRide(
                request.riderId(),
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

    @GetMapping
    public List<RideResponse> getRides() {
        return rideService.getAllRides().stream()
                .map(RideResponse::from)
                .toList();
    }

    @GetMapping("/status/{status}")
    public List<RideResponse> getRidesByStatus(@PathVariable RideStatus status) {
        return rideService.getRidesByStatus(status).stream()
                .map(RideResponse::from)
                .toList();
    }

    @PostMapping("/{id}/cancel")
    public RideResponse cancelRide(@PathVariable Long id) {
        return RideResponse.from(rideService.cancelRide(id));
    }
}
