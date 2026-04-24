package com.taxol760.services;

import com.taxol760.database.models.driver.DriverModel;
import com.taxol760.database.models.driver.DriverStatus;
import com.taxol760.database.models.user.UserModel;
import com.taxol760.database.repositories.DriverRepository;
import com.taxol760.database.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public DriverModel createDriver(Long userId, String licenseNumber) {
        if (driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("License number is already in use");
        }

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        DriverModel driver = new DriverModel();
        driver.setUser(user);
        driver.setLicenseNumber(licenseNumber);
        driver.setStatus(DriverStatus.PENDING);

        return driverRepository.save(driver);
    }

    public DriverModel updateDriverStatus(Long id, DriverStatus status) {
        DriverModel driver = getDriver(id);
        driver.setStatus(status);
        return driverRepository.save(driver);
    }

    @Transactional(readOnly = true)
    public DriverModel getDriver(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));
    }

    @Transactional(readOnly = true)
    public DriverModel getDriverByUser(UserModel user) {
        return driverRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));
    }

    @Transactional(readOnly = true)
    public DriverModel getDriverByLicenseNumber(String licenseNumber) {
        return driverRepository.findByLicenseNumber(licenseNumber)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));
    }

    @Transactional(readOnly = true)
    public List<DriverModel> getDriversByStatus(DriverStatus status) {
        return driverRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<DriverModel> getAllDrivers() {
        return driverRepository.findAll();
    }
}
