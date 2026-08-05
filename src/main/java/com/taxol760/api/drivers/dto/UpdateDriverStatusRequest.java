package com.taxol760.api.drivers.dto;

import com.taxol760.store.model.driver.DriverStatus;

public record UpdateDriverStatusRequest(
        DriverStatus status
) {
}
