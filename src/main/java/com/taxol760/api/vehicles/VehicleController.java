package com.taxol760.api.vehicles;

import com.taxol760.api.vehicles.dto.CreateVehicleRequest;
import com.taxol760.api.vehicles.dto.VehicleResponse;
import com.taxol760.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(@RequestBody CreateVehicleRequest request) {
        return VehicleResponse.from(vehicleService.createVehicle(
                request.driverId(),
                request.brand(),
                request.model(),
                request.color(),
                request.plateNumber()
        ));
    }

    @GetMapping("/driver/{driverId}")
    public VehicleResponse getVehicleByDriver(@PathVariable Long driverId) {
        return VehicleResponse.from(vehicleService.getVehicleByDriverId(driverId));
    }
}
