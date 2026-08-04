package com.taxol760.api.drivers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateDriverRequest(
        @Schema(example = "LIC-001")
        String licenseNumber,

        @Schema(example = "Toyota")
        String vehicleBrand,

        @Schema(example = "Camry")
        String vehicleModel,

        @Schema(example = "Black")
        String vehicleColor,

        @Schema(example = "ABC-1234")
        String vehiclePlateNumber
) {}
