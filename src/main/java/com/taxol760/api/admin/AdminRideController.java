package com.taxol760.api.admin;

import com.taxol760.api.rides.dto.RideResponse;
import com.taxol760.databaseANDcache.model.ride.RideStatus;
import com.taxol760.service.ride.RideService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/rides")
@RequiredArgsConstructor
public class AdminRideController {
    private final RideService rideService;

    @GetMapping
    public List<RideResponse> getRides() {
        return rideService.getAllRides().stream()
                .map(RideResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public RideResponse getRide(@PathVariable Long id) {
        return RideResponse.from(rideService.getRide(id));
    }

    @GetMapping("/status/{status}")
    public List<RideResponse> getRidesByStatus(@PathVariable RideStatus status) {
        return rideService.getRidesByStatus(status).stream()
                .map(RideResponse::from)
                .toList();
    }

    @GetMapping("/rider/{riderId}")
    public List<RideResponse> getRidesByRider(@PathVariable Long riderId) {
        return rideService.getRidesByRiderId(riderId).stream()
                .map(RideResponse::from)
                .toList();
    }

    @GetMapping("/driver/{driverId}")
    public List<RideResponse> getRidesByDriver(@PathVariable Long driverId) {
        return rideService.getRidesByDriverId(driverId).stream()
                .map(RideResponse::from)
                .toList();
    }
}
