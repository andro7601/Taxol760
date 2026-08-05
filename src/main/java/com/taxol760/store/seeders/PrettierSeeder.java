package com.taxol760.store.seeders;

import com.taxol760.store.model.driver.DriverModel;
import com.taxol760.store.model.driver.DriverStatus;
import com.taxol760.store.model.user.UserModel;
import com.taxol760.store.model.user.UserRole;
import com.taxol760.store.model.vehicle.VehicleModel;
import com.taxol760.store.repository.DriverRepository;
import com.taxol760.store.repository.UserRepository;
import com.taxol760.store.repository.VehicleRepository;
import com.taxol760.service.auth.JwtService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PrettierSeeder implements CommandLineRunner {
    private static final String USER_PASSWORD = "user123";
    private static final String DRIVER_PASSWORD = "driver123";

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void run(String... args) {
        UserModel userOne = seedUser("seed.user1@taxol.test", "Seed User One", USER_PASSWORD, "5551001", UserRole.USER);
        UserModel userTwo = seedUser("seed.user2@taxol.test", "Seed User Two", USER_PASSWORD, "5551002", UserRole.USER);
        UserModel driverOne = seedUser("seed.driver1@taxol.test", "Seed Driver One", DRIVER_PASSWORD, "5552001", UserRole.DRIVER);
        UserModel driverTwo = seedUser("seed.driver2@taxol.test", "Seed Driver Two", DRIVER_PASSWORD, "5552002", UserRole.DRIVER);
        UserModel driverThree = seedUser("seed.driver3@taxol.test", "Seed Driver Three", DRIVER_PASSWORD, "5552003", UserRole.DRIVER);

        DriverModel seededDriverOne = seedDriver(driverOne, "SEED-LIC-001");
        DriverModel seededDriverTwo = seedDriver(driverTwo, "SEED-LIC-002");
        DriverModel seededDriverThree = seedDriver(driverThree, "SEED-LIC-003");

        seedVehicle(seededDriverOne, "Toyota", "Prius", "White", "TAX-20");
        seedVehicle(seededDriverTwo, "Toyota", "Prius", "White", "TAX-21");
        seedVehicle(seededDriverThree, "Toyota", "Prius", "White", "TAX-22");

        printSeededAccounts(List.of(userOne, userTwo, driverOne, driverTwo, driverThree));
    }

    private UserModel seedUser(String email, String name, String plainPassword, String phoneNumber, UserRole role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserModel user = new UserModel();
                    user.setEmail(email);
                    user.setName(name);
                    user.setPassword(passwordEncoder.encode(plainPassword));
                    user.setPhoneNumber(phoneNumber);
                    user.setRole(role);
                    return userRepository.save(user);
                });
    }

    private DriverModel seedDriver(UserModel user, String licenseNumber) {
        return driverRepository.findByUser(user)
                .orElseGet(() -> {
                    DriverModel driver = new DriverModel();
                    driver.setUser(user);
                    driver.setLicenseNumber(licenseNumber);
                    driver.setStatus(DriverStatus.APPROVED);
                    return driverRepository.save(driver);
                });
    }

    private void seedVehicle(DriverModel driver, String brand, String model, String color, String plateNumber) {
        if (vehicleRepository.findByDriver(driver).isPresent()) {
            return;
        }

        VehicleModel vehicle = new VehicleModel();
        vehicle.setDriver(driver);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setColor(color);
        vehicle.setPlateNumber(plateNumber);
        vehicleRepository.save(vehicle);
    }

    private void printSeededAccounts(List<UserModel> users) {
        System.out.println();
        System.out.println("Seeded Taxol760 accounts");
        System.out.println("Plain passwords:");
        System.out.println("users: " + USER_PASSWORD);
        System.out.println("drivers: " + DRIVER_PASSWORD);
        System.out.println();
        System.out.println("JWTs:");
        users.forEach(user -> System.out.println(user.getRole() + " " + user.getEmail() + " = " + jwtService.generateToken(user)));
        System.out.println();
    }
}
