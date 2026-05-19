package com.sportlink.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlink.account.dto.request.RegisterUserRequest;
import com.sportlink.account.dto.response.UserProfileResponse;
import com.sportlink.account.service.UserService;
import com.sportlink.account.model.enums.SkillLevel;
import com.sportlink.account.model.enums.UserRole;
import com.sportlink.account.model.enums.UserStatus;
import com.sportlink.account.exception.GlobalExceptionHandler;
import com.sportlink.account.exception.ResourceNotFoundException;
import com.sportlink.account.security.JwtAuthenticationFilter;
import com.sportlink.account.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void registerUser_returns201_whenValidRequest() throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .name("TestName TestSurname")
                .email("testname@example.com")
                .password("password123")
                .role(UserRole.USER)
                .skillLevel(SkillLevel.INTERMEDIATE)
                .city("Tartu")
                .build();

        UUID generatedId = UUID.randomUUID();
        UserProfileResponse mockResponse = UserProfileResponse.builder()
                .userId(generatedId)
                .name("TestName TestSurname")
                .email("testname@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .skillLevel(SkillLevel.INTERMEDIATE)
                .city("Tartu")
                .build();

        when(userService.registerUser(any(RegisterUserRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(generatedId.toString()))
                .andExpect(jsonPath("$.email").value("testname@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getUser_returns404_whenUserNotFound() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(userService.getUserById(missingId))
                .thenThrow(new ResourceNotFoundException("User not found: " + missingId));

        mockMvc.perform(get("/api/users/{id}", missingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: " + missingId));
    }
}
