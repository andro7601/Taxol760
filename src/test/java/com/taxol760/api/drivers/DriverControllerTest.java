package com.taxol760.api.drivers;

import static com.taxol760.TestModels.driver;
import static com.taxol760.TestModels.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taxol760.api.ControllerTestBase;
import com.taxol760.database.model.driver.DriverStatus;
import com.taxol760.database.model.user.UserRole;
import com.taxol760.service.driver.DriverService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest extends ControllerTestBase {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverService driverService;

    @Test
    void createDriverReturnsCreatedDriver() throws Exception {
        when(driverService.createDriver(eq(1L), any())).thenReturn(driver(10L, user(1L, UserRole.DRIVER), DriverStatus.PENDING));

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "licenseNumber": "LIC-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getDriversByStatusReturnsDrivers() throws Exception {
        when(driverService.getDriversByStatus(DriverStatus.APPROVED))
                .thenReturn(List.of(driver(11L, user(2L, UserRole.DRIVER), DriverStatus.APPROVED)));

        mockMvc.perform(get("/api/drivers/status/APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void updateDriverStatusReturnsUpdatedDriver() throws Exception {
        when(driverService.updateDriverStatus(10L, DriverStatus.APPROVED))
                .thenReturn(driver(10L, user(1L, UserRole.DRIVER), DriverStatus.APPROVED));

        mockMvc.perform(patch("/api/drivers/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
