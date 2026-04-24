package com.taxol760.database.repositories;

import com.taxol760.database.models.driver.DriverModel;
import com.taxol760.database.models.vehicle.VehicleModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleModel, Long> {
    Optional<VehicleModel> findByDriver(DriverModel driver);

    Optional<VehicleModel> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);
}
