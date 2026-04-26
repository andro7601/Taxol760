package com.taxol760.api.drivers.dto;

public record CreateDriverRequest(
        Long userId,
        String licenseNumber
) {
}
