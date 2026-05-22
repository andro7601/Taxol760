package com.taxol760.service.driver;

import com.taxol760.databaseANDcache.cache.CachedDriverInfo;
import com.taxol760.databaseANDcache.cache.cacheservice;
import com.taxol760.databaseANDcache.model.driver.DriverModel;
import com.taxol760.databaseANDcache.model.driver.DriverStatus;
import com.taxol760.databaseANDcache.model.user.UserModel;
import com.taxol760.databaseANDcache.repository.DriverRepository;
import com.taxol760.databaseANDcache.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DriverService {
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final cacheservice cacheservice;

    public DriverModel createDriver(Long userId, String licenseNumber) {
        if (driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("License number is already in use");
        }

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        DriverModel driver = new DriverModel();
        driver.setUser(user);
        driver.setLicenseNumber(licenseNumber);
        driver.setStatus(DriverStatus.APPROVED);

        return driverRepository.save(driver);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public DriverModel updateDriverStatus(Long id, DriverStatus status) {
        DriverModel driver = getDriver(id);
        driver.setStatus(status);
        DriverModel savedDriver = driverRepository.save(driver);
        cacheservice.refreshDriverInfo(CachedDriverInfo.from(savedDriver));
        return savedDriver;
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
    public DriverModel getDriverByUserId(Long userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return getDriverByUser(user);
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

    @Transactional(readOnly = true)
    public void goOnline(int id) {
        DriverModel driver = getDriver((long) id);
        cacheservice.addDriver(CachedDriverInfo.from(driver), 70, 67);
    }

    public void goOffline(int id) {
        cacheservice.delDriver(id);
    }

    public void updateLocation(int id, double lon, double lat) {
        CachedDriverInfo driverInfo = cacheservice.getCachedDriverInfo(id);
        if (driverInfo == null) {
            driverInfo = CachedDriverInfo.from(getDriver((long) id));
        }

        cacheservice.updateDriver(driverInfo, lon, lat);
    }

    @Transactional(readOnly = true)
    public List<CachedDriverInfo> suggestedDrivers(double lon, double lat) {
        List<CachedDriverInfo> suggestedDrivers = cacheservice.suggestDrivers(lon, lat);
        if(suggestedDrivers.isEmpty()){
            throw new EntityNotFoundException("Drivers not found");
        }
        return suggestedDrivers;
    }
}
