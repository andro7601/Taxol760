package com.taxol760.database.repositories;

import com.taxol760.database.models.driver.DriverModel;
import com.taxol760.database.models.driver.DriverStatus;
import com.taxol760.database.models.user.UserModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<DriverModel, Long> {
    Optional<DriverModel> findByUser(UserModel user);

    Optional<DriverModel> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    List<DriverModel> findByStatus(DriverStatus status);
}
