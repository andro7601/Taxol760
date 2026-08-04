package com.taxol760.api.vehicles;

import com.taxol760.api.vehicles.dto.VehicleResponse;
import com.taxol760.service.auth.CurrentUserService;
import com.taxol760.service.driver.DriverService;
import com.taxol760.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;
    private final DriverService driverService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public VehicleResponse getMyVehicle() {
        Long userId = currentUserService.getCurrentUserId();
        Long driverId = driverService.getDriverByUserId(userId).getId();
        return VehicleResponse.from(vehicleService.getVehicleByDriverId(driverId));
    }
}
