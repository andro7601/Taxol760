package com.taxol760.api.rides;

import static com.taxol760.TestModels.driver;
import static com.taxol760.TestModels.ride;
import static com.taxol760.TestModels.user;
import static com.taxol760.TestModels.vehicle;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taxol760.api.ControllerTestBase;
import com.taxol760.database.model.driver.DriverModel;
import com.taxol760.database.model.driver.DriverStatus;
import com.taxol760.database.model.ride.RideModel;
import com.taxol760.database.model.ride.RideStatus;
import com.taxol760.database.model.user.UserModel;
import com.taxol760.database.model.user.UserRole;
import com.taxol760.database.model.vehicle.VehicleModel;
import com.taxol760.service.ride.RideService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RideController.class)
@AutoConfigureMockMvc(addFilters = false)
class RideControllerTest extends ControllerTestBase {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @Test
    void requestRideReturnsCreatedRide() throws Exception {
        UserModel rider = user(1L, UserRole.USER);
        when(rideService.requestRide(eq(1L), any(), any(), any(), any()))
                .thenReturn(ride(30L, rider, null, null, RideStatus.REQUESTED));

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "riderId": 1,
                                  "pickupLatitude": 41.7151,
                                  "pickupLongitude": 44.8271,
                                  "dropoffLatitude": 41.7251,
                                  "dropoffLongitude": 44.8371
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(30))
                .andExpect(jsonPath("$.riderId").value(1))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void acceptRideReturnsAcceptedRide() throws Exception {
        RideModel acceptedRide = assignedRide(RideStatus.ACCEPTED);
        when(rideService.acceptRide(30L, 10L)).thenReturn(acceptedRide);

        mockMvc.perform(post("/api/rides/30/accept").param("driverId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(10))
                .andExpect(jsonPath("$.vehicleId").value(20))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void startRideReturnsInProgressRide() throws Exception {
        when(rideService.startRide(30L)).thenReturn(assignedRide(RideStatus.IN_PROGRESS));

        mockMvc.perform(post("/api/rides/30/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getRidesByDriverReturnsRides() throws Exception {
        when(rideService.getRidesByDriverId(10L)).thenReturn(List.of(assignedRide(RideStatus.ACCEPTED)));

        mockMvc.perform(get("/api/rides/driver/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value(10))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    private RideModel assignedRide(RideStatus status) {
        UserModel rider = user(1L, UserRole.USER);
        DriverModel driver = driver(10L, user(2L, UserRole.DRIVER), DriverStatus.APPROVED);
        VehicleModel vehicle = vehicle(20L, driver);
        return ride(30L, rider, driver, vehicle, status);
    }
}
