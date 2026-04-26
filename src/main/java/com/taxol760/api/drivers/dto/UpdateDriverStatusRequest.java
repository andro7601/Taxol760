package com.taxol760.api.drivers.dto;

import com.taxol760.database.model.driver.DriverStatus;

public record UpdateDriverStatusRequest(
        DriverStatus status
) {
}
