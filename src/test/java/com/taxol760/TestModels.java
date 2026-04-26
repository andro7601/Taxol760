package com.taxol760;

import com.taxol760.database.model.driver.DriverModel;
import com.taxol760.database.model.driver.DriverStatus;
import com.taxol760.database.model.ride.RideModel;
import com.taxol760.database.model.ride.RideStatus;
import com.taxol760.database.model.user.UserModel;
import com.taxol760.database.model.user.UserRole;
import com.taxol760.database.model.vehicle.VehicleModel;
import java.time.LocalDateTime;

public final class TestModels {
    private TestModels() {
    }

    public static UserModel user(Long id, UserRole role) {
        UserModel user = new UserModel();
        user.setId(id);
        user.setEmail("user" + id + "@taxol.test");
        user.setName("User " + id);
        user.setPassword("encoded-password");
        user.setPhoneNumber("555000" + id);
        user.setRole(role);
        return user;
    }

    public static DriverModel driver(Long id, UserModel user, DriverStatus status) {
        DriverModel driver = new DriverModel();
        driver.setId(id);
        driver.setUser(user);
        driver.setLicenseNumber("LIC-" + id);
        driver.setStatus(status);
        return driver;
    }

    public static VehicleModel vehicle(Long id, DriverModel driver) {
        VehicleModel vehicle = new VehicleModel();
        vehicle.setId(id);
        vehicle.setDriver(driver);
        vehicle.setBrand("Toyota");
        vehicle.setModel("Prius");
        vehicle.setColor("White");
        vehicle.setPlateNumber("TAX-" + id);
        return vehicle;
    }

    public static RideModel ride(Long id, UserModel rider, DriverModel driver, VehicleModel vehicle, RideStatus status) {
        RideModel ride = new RideModel();
        ride.setId(id);
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setVehicle(vehicle);
        ride.setPickupLatitude(41.7151);
        ride.setPickupLongitude(44.8271);
        ride.setDropoffLatitude(41.7251);
        ride.setDropoffLongitude(44.8371);
        ride.setStatus(status);
        ride.setCreatedAt(LocalDateTime.of(2026, 4, 26, 12, 0));
        return ride;
    }
}
