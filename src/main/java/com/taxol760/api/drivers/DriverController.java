package com.taxol760.api.drivers;

import com.taxol760.api.drivers.dto.CreateDriverRequest;
import com.taxol760.api.drivers.dto.DriverResponse;
import com.taxol760.api.drivers.dto.UpdateDriverLocationRequest;
import com.taxol760.api.drivers.dto.UpdateDriverStatusRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or @resourceAccess.isCurrentUser(#request.userId())")
    public DriverResponse createDriver(@RequestBody CreateDriverRequest request) {
        return DriverResponse.from(driverService.createDriver(
                request.userId(),
                request.licenseNumber()
        ));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "drivers", key = "#id")
    public DriverResponse getDriver(@PathVariable Long id) {
        return DriverResponse.from(driverService.getDriver(id));
    }

    @GetMapping("/user/{userId}")
    public DriverResponse getDriverByUser(@PathVariable Long userId) {
        return DriverResponse.from(driverService.getDriverByUserId(userId));
    }

    @GetMapping("/license/{licenseNumber}")
    public DriverResponse getDriverByLicenseNumber(@PathVariable String licenseNumber) {
        return DriverResponse.from(driverService.getDriverByLicenseNumber(licenseNumber));
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
