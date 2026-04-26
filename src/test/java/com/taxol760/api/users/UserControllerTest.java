package com.taxol760.api.users;

import static com.taxol760.TestModels.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taxol760.api.ControllerTestBase;
import com.taxol760.database.model.user.UserRole;
import com.taxol760.service.user.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest extends ControllerTestBase {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        when(userService.createUser(any(), any(), any(), any(), any())).thenReturn(user(1L, UserRole.USER));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user1@taxol.test",
                                  "name": "User 1",
                                  "password": "password123",
                                  "phoneNumber": "5550001",
                                  "role": "USER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@taxol.test"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getUsersByRoleReturnsMatchingUsers() throws Exception {
        when(userService.getUsersByRole(UserRole.DRIVER)).thenReturn(List.of(user(2L, UserRole.DRIVER)));

        mockMvc.perform(get("/api/users/role/DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].role").value("DRIVER"));
    }

    @Test
    void deleteUserReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(eq(1L));

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
