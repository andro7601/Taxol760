package com.taxol760.database.model.ride;

import com.taxol760.database.model.driver.DriverModel;
import com.taxol760.database.model.user.UserModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.taxol760.database.model.vehicle.VehicleModel;

@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rider_id", nullable = false)
    private UserModel rider;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverModel driver;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private VehicleModel vehicle;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    @Column(nullable = false)
    private Double dropoffLatitude;

    @Column(nullable = false)
    private Double dropoffLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
