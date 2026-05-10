package com.taxol760.api.admin;

import com.taxol760.api.vehicles.dto.VehicleResponse;
import com.taxol760.service.vehicle.VehicleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/vehicles")
@RequiredArgsConstructor
public class AdminVehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public List<VehicleResponse> getVehicles() {
        return vehicleService.getAllVehicles().stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicle(@PathVariable Long id) {
        return VehicleResponse.from(vehicleService.getVehicle(id));
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
