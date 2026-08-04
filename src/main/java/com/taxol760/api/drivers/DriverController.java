package com.taxol760.api.drivers;

import com.taxol760.api.drivers.dto.CreateDriverRequest;
import com.taxol760.api.drivers.dto.DriverResponse;
import com.taxol760.api.drivers.dto.UpdateDriverLocationRequest;
import com.taxol760.api.drivers.dto.UpdateDriverStatusRequest;
import com.taxol760.service.auth.CurrentUserService;
import com.taxol760.service.driver.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;
    private final CurrentUserService currentUserService;

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse createDriverForCurrentUser(@RequestBody CreateDriverRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return DriverResponse.from(driverService.createAndUpgradeToDriver(
                userId,
                request.licenseNumber(),
                request.vehicleBrand(),
                request.vehicleModel(),
                request.vehicleColor(),
                request.vehiclePlateNumber()
        ));
    }

    @GetMapping("/me")
    public DriverResponse getDriverForCurrentUser() {
        return DriverResponse.from(driverService.getDriverByUserId(currentUserService.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "drivers", key = "#id")
    public DriverResponse getDriver(@PathVariable Long id) {
        return DriverResponse.from(driverService.getDriver(id));
    }

    @PatchMapping("/{id}/status")
    public DriverResponse updateDriverStatus(
            @PathVariable Long id,
            @RequestBody UpdateDriverStatusRequest request
    ) {
        return DriverResponse.from(driverService.updateDriverStatus(id, request.status()));
    }

    @PutMapping("/{id}/online")
    public void goOnline(@PathVariable int id) {
        driverService.goOnline(id);
    }

    @PutMapping("/{id}/offline")
    public void goOffline(@PathVariable int id) {
        driverService.goOffline(id);
    }

    @PutMapping("/{id}/location")
    public UpdateDriverLocationRequest updateLocation(
            @PathVariable int id,
            @RequestBody UpdateDriverLocationRequest request
    ) {
        driverService.updateLocation(id, request.longitude(), request.latitude());
        return request;
    }

    @GetMapping("/suggestions")
    public List<DriverResponse> suggestions(
            @RequestParam double longitude,
            @RequestParam double latitude
    ) {
       return driverService.suggestedDrivers(longitude, latitude)
               .stream()
               .map(DriverResponse::from)
               .toList();
    }
}
