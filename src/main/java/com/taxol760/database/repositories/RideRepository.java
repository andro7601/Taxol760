package com.taxol760.database.repositories;

import com.taxol760.database.models.driver.DriverModel;
import com.taxol760.database.models.ride.RideModel;
import com.taxol760.database.models.ride.RideStatus;
import com.taxol760.database.models.user.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<RideModel, Long> {
    List<RideModel> findByRider(UserModel rider);

    List<RideModel> findByDriver(DriverModel driver);

    List<RideModel> findByStatus(RideStatus status);

    List<RideModel> findByRiderAndStatus(UserModel rider, RideStatus status);

    List<RideModel> findByDriverAndStatus(DriverModel driver, RideStatus status);
}
