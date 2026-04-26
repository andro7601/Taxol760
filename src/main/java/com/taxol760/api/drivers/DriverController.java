package com.taxol760.api.drivers;

import com.taxol760.api.drivers.dto.CreateDriverRequest;
import com.taxol760.api.drivers.dto.DriverResponse;
import com.taxol760.api.drivers.dto.UpdateDriverStatusRequest;
import com.taxol760.database.model.driver.DriverStatus;
import com.taxol760.service.driver.DriverService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse createDriver(@RequestBody CreateDriverRequest request) {
        return DriverResponse.from(driverService.createDriver(
                request.userId(),
                request.licenseNumber()
        ));
    }

    @GetMapping("/{id}")
    public DriverResponse getDriver(@PathVariable Long id) {
        return DriverResponse.from(driverService.getDriver(id));
    }

    @GetMapping
    public List<DriverResponse> getDrivers() {
        return driverService.getAllDrivers().stream()
                .map(DriverResponse::from)
                .toList();
    }

    @GetMapping("/user/{userId}")
    public DriverResponse getDriverByUser(@PathVariable Long userId) {
        return DriverResponse.from(driverService.getDriverByUserId(userId));
    }

    @GetMapping("/license/{licenseNumber}")
    public DriverResponse getDriverByLicenseNumber(@PathVariable String licenseNumber) {
        return DriverResponse.from(driverService.getDriverByLicenseNumber(licenseNumber));
    }

    @GetMapping("/status/{status}")
    public List<DriverResponse> getDriversByStatus(@PathVariable DriverStatus status) {
        return driverService.getDriversByStatus(status).stream()
                .map(DriverResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/status")
    public DriverResponse updateDriverStatus(
            @PathVariable Long id,
            @RequestBody UpdateDriverStatusRequest request
    ) {
        return DriverResponse.from(driverService.updateDriverStatus(id, request.status()));
    }
}
