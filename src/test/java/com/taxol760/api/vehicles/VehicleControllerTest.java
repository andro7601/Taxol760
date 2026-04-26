package com.taxol760.api.vehicles;

import static com.taxol760.TestModels.driver;
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
import com.taxol760.database.model.user.UserRole;
import com.taxol760.service.vehicle.VehicleService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest extends ControllerTestBase {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @Test
    void createVehicleReturnsCreatedVehicle() throws Exception {
        DriverModel driver = driver(10L, user(1L, UserRole.DRIVER), DriverStatus.APPROVED);
        when(vehicleService.createVehicle(eq(10L), any(), any(), any(), any())).thenReturn(vehicle(20L, driver));

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "driverId": 10,
                                  "brand": "Toyota",
                                  "model": "Prius",
                                  "color": "White",
                                  "plateNumber": "TAX-20"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.driverId").value(10))
                .andExpect(jsonPath("$.plateNumber").value("TAX-20"));
    }

    @Test
    void getVehiclesReturnsVehicles() throws Exception {
        DriverModel driver = driver(10L, user(1L, UserRole.DRIVER), DriverStatus.APPROVED);
        when(vehicleService.getAllVehicles()).thenReturn(List.of(vehicle(20L, driver)));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].driverId").value(10));
    }

    @Test
    void getVehicleByDriverReturnsVehicle() throws Exception {
        DriverModel driver = driver(10L, user(1L, UserRole.DRIVER), DriverStatus.APPROVED);
        when(vehicleService.getVehicleByDriverId(10L)).thenReturn(vehicle(20L, driver));

        mockMvc.perform(get("/api/vehicles/driver/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }
}
