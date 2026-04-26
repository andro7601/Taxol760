package com.taxol760.api.vehicles;

import com.taxol760.api.vehicles.dto.CreateVehicleRequest;
import com.taxol760.api.vehicles.dto.VehicleResponse;
import com.taxol760.service.vehicle.VehicleService;
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

    @GetMapping("/{id}")
    public VehicleResponse getVehicle(@PathVariable Long id) {
        return VehicleResponse.from(vehicleService.getVehicle(id));
    }

    @GetMapping
    public List<VehicleResponse> getVehicles() {
        return vehicleService.getAllVehicles().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @GetMapping("/driver/{driverId}")
    public VehicleResponse getVehicleByDriver(@PathVariable Long driverId) {
        return VehicleResponse.from(vehicleService.getVehicleByDriverId(driverId));
    }

    @GetMapping("/plate/{plateNumber}")
    public VehicleResponse getVehicleByPlateNumber(@PathVariable String plateNumber) {
        return VehicleResponse.from(vehicleService.getVehicleByPlateNumber(plateNumber));
    }
}
