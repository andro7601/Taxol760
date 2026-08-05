package com.taxol760.service.driver;

import com.taxol760.store.cache.CachedDriverInfo;
import com.taxol760.store.cache.CacheService;
import com.taxol760.store.model.driver.DriverModel;
import com.taxol760.store.model.driver.DriverStatus;
import com.taxol760.store.model.user.UserModel;
import com.taxol760.store.model.user.UserRole;
import com.taxol760.store.model.vehicle.VehicleModel;
import com.taxol760.store.repository.DriverRepository;
import com.taxol760.store.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import com.taxol760.store.repository.VehicleRepository;

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
    private final CacheService cacheService;
    private final VehicleRepository vehicleRepository;


    public DriverModel createAndUpgradeToDriver(Long userId, String licenseNumber,
            String brand, String model, String color, String plateNumber) {

        if (driverRepository.existsByLicenseNumber(licenseNumber)) {
            throw new IllegalArgumentException("License number is already in use");
        }
        if (vehicleRepository.existsByPlateNumber(plateNumber)) {
            throw new IllegalArgumentException("Plate number is already in use");
        }

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setRole(UserRole.DRIVER);
        userRepository.save(user);

        DriverModel driver = new DriverModel();
        driver.setUser(user);
        driver.setLicenseNumber(licenseNumber);
        driver.setStatus(DriverStatus.APPROVED);
        DriverModel savedDriver = driverRepository.save(driver);

        VehicleModel vehicle = new VehicleModel();
        vehicle.setDriver(savedDriver);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setColor(color);
        vehicle.setPlateNumber(plateNumber);
        vehicleRepository.save(vehicle);

        return savedDriver;
    }

    public DriverModel updateDriverStatus(Long id, DriverStatus status) {
        DriverModel driver = getDriver(id);
        driver.setStatus(status);
        DriverModel savedDriver = driverRepository.save(driver);
        VehicleModel vehicle = getVehicle(savedDriver);
        cacheService.refreshDriverInfo(CachedDriverInfo.from(savedDriver, vehicle));
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
        VehicleModel vehicle = getVehicle(driver);
        cacheService.addDriver(CachedDriverInfo.from(driver, vehicle), 70, 67);
    }

    public void goOffline(int id) {
        cacheService.delDriver(id);
    }

    public void updateLocation(int driverId, double lon, double lat) {
        int id = driverId;
        CachedDriverInfo driverInfo = cacheService.getCachedDriverInfo(id);
        if (driverInfo == null) {
            DriverModel driver = getDriver((long) id);
            VehicleModel vehicle = getVehicle(driver);
            driverInfo = CachedDriverInfo.from(driver, vehicle);
        }

        cacheService.updateDriver(driverInfo, lon, lat);
    }

    @Transactional(readOnly = true)
    public List<CachedDriverInfo> suggestedDrivers(double lon, double lat) {
        List<CachedDriverInfo> suggestedDrivers = cacheService.suggestDrivers(lon, lat);
        if(suggestedDrivers.isEmpty()){
            throw new EntityNotFoundException("Drivers not found");
        }
        return suggestedDrivers;
    }

    private VehicleModel getVehicle(DriverModel driver) {
        return vehicleRepository.findByDriver(driver)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found for driver " + driver.getId()));
    }
}
