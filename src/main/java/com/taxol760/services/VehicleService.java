package com.taxol760.services;

import com.taxol760.database.models.driver.DriverModel;
import com.taxol760.database.models.vehicle.VehicleModel;
import com.taxol760.database.repositories.DriverRepository;
import com.taxol760.database.repositories.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public VehicleModel createVehicle(
            Long driverId,
            String brand,
            String model,
            String color,
            String plateNumber
    ) {
        if (vehicleRepository.existsByPlateNumber(plateNumber)) {
            throw new IllegalArgumentException("Plate number is already in use");
        }

        DriverModel driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));

        VehicleModel vehicle = new VehicleModel();
        vehicle.setDriver(driver);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setColor(color);
        vehicle.setPlateNumber(plateNumber);

        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleModel getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
    }

    @Transactional(readOnly = true)
    public VehicleModel getVehicleByDriver(DriverModel driver) {
        return vehicleRepository.findByDriver(driver)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
    }

    @Transactional(readOnly = true)
    public VehicleModel getVehicleByPlateNumber(String plateNumber) {
        return vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found"));
    }

    @Transactional(readOnly = true)
    public List<VehicleModel> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}
