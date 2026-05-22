package com.taxol760.api.drivers.dto;

import com.taxol760.databaseANDcache.model.driver.DriverStatus;

public record UpdateDriverStatusRequest(
        DriverStatus status
) {
}
