package com.taxol760.store.repository;

import com.taxol760.store.model.driver.DriverModel;
import com.taxol760.store.model.ride.RideModel;
import com.taxol760.store.model.ride.RideStatus;
import com.taxol760.store.model.user.UserModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<RideModel, Long> {
    List<RideModel> findByRider(UserModel rider);

    List<RideModel> findByDriver(DriverModel driver);

    List<RideModel> findByStatus(RideStatus status);

    List<RideModel> findByRiderAndStatus(UserModel rider, RideStatus status);

    List<RideModel> findByDriverAndStatus(DriverModel driver, RideStatus status);
}
