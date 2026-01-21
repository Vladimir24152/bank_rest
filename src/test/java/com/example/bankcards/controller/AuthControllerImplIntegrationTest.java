package com.example.bankcards.controller;

import com.example.bankcards.BaseIntegrationTest;
import com.example.bankcards.dto.request.SignInRequest;
import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private static final String SIGN_UP_URL = "/api/v1/auth/sign-up";
    private static final String SIGN_IN_URL = "/api/v1/auth/sign-in";
    private static final String GIVE_ADMIN_URL = "/api/v1/auth/give-admin/";

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Регистрация нового пользователя - успешная регистрация")
    void whenSignUpValidUserThenReturnJwtToken() throws Exception {
        SignUpRequest request = new SignUpRequest("newuser", "password123");

        mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());

        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("newuser", savedUser.getUsername());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    @DisplayName("Регистрация пользователя с существующим именем - возвращает ошибку")
    void whenSignUpWithExistingUsernameThenReturnConflict() throws Exception {
        User existingUser = User.builder()
                .username("existinguser")
                .password("password123")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(existingUser);

        SignUpRequest request = new SignUpRequest("existinguser", "password456");

        mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь с именем 'existinguser' уже существует"));
    }

    @Test
    @DisplayName("Авторизация с правильными учетными данными - возвращает JWT токен")
    void whenSignInWithValidCredentialsThenReturnJwtToken() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest("authuser", "password123");

        mockMvc.perform(post(SIGN_UP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest("authuser", "password123");

        mockMvc.perform(post(SIGN_IN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Авторизация с неверным паролем - возвращает ошибку 403")
    void whenSignInWithWrongPasswordThenReturnForbidden() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest("wrongpassuser", "correctpassword");

        mockMvc.perform(post(SIGN_UP_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        SignInRequest signInRequest = new SignInRequest("wrongpassuser", "wrongpassword");

        mockMvc.perform(post(SIGN_IN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Авторизация несуществующего пользователя - возвращает ошибку 403")
    void whenSignInNonExistingUserThenReturnForbidden() throws Exception {
        SignInRequest signInRequest = new SignInRequest("nonexisting", "password123");

        mockMvc.perform(post(SIGN_IN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Назначение роли ADMIN администратором - успешное выполнение")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminGivesAdminRoleThenSuccess() throws Exception {
        User regularUser = User.builder()
                .username("regularuser")
                .password("password123")
                .role(Role.ROLE_USER)
                .build();
        User savedUser = userRepository.save(regularUser);
        Long userId = savedUser.getId();

        mockMvc.perform(post(GIVE_ADMIN_URL + "user/" +userId))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals(Role.ROLE_ADMIN, updatedUser.getRole());
    }

    @Test
    @DisplayName("Назначение роли ADMIN несуществующему пользователю - возвращает ошибку 404")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenGiveAdminToNonExistingUserThenReturnNotFound() throws Exception {
        Long nonExistingUserId = 999L;

        mockMvc.perform(post(GIVE_ADMIN_URL + nonExistingUserId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Регистрация с некорректными данными - возвращает ошибку валидации")
    void whenSignUpWithInvalidDataThenReturnBadRequest() throws Exception {
        SignUpRequest request = new SignUpRequest("", "password123");

        mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Авторизация с некорректными данными - возвращает ошибку валидации")
    void whenSignInWithInvalidDataThenReturnBadRequest() throws Exception {
        SignInRequest request = new SignInRequest("testuser", "");

        mockMvc.perform(post(SIGN_IN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}