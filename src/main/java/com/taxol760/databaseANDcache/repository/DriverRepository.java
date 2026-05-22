package com.taxol760.databaseANDcache.repository;

import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.driver.DriverStatus;
import com.taxol760.databaseANDcache.model.user.UserModel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverRepository extends JpaRepository<DriverModel, Long> {
    Optional<DriverModel> findByUser(UserModel user);

    Optional<DriverModel> findByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    List<DriverModel> findByStatus(DriverStatus status);

    List<DriverModel> id(Long id);
}
