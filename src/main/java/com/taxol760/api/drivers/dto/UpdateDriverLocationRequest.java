package com.taxol760.api.drivers.dto;

public record UpdateDriverLocationRequest(
        double longitude,
        double latitude
) {
}
