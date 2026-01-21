package com.example.bankcards.service;

import com.example.bankcards.dto.request.SignInRequest;
import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.User;
import com.example.bankcards.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.bankcards.dto.response.JwtAuthenticationResponse;
import com.example.bankcards.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private SignUpRequest signUpRequest;
    private SignInRequest signInRequest;
    private User testUser;
    private String testJwtToken;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        signUpRequest = SignUpRequest.builder()
                .username("testUser")
                .password("testPassword123")
                .build();

        signInRequest = SignInRequest.builder()
                .username("testUser")
                .password("testPassword123")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();

        testJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0VXNlciIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }


    @Test
    @DisplayName("Успешная регистрация пользователя")
    void whenSignUpValidRequestThenReturnsJwtResponse() {
        String encodedPassword = "encodedPassword123";

        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(User.class))).thenReturn(testJwtToken);

        JwtAuthenticationResponse response = authenticationService.signUp(signUpRequest);

        assertNotNull(response);
        assertEquals(testJwtToken, response.getToken());
    }

    @Test
    @DisplayName("При регистрации назначается роль USER")
    void  whenSignUpUserHasCorrectRole() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn(testJwtToken);

        authenticationService.signUp(signUpRequest);

        verify(userService).create(argThat(user ->
                user.getRole() == Role.ROLE_USER
        ));
    }

    @Test
    @DisplayName("Успешная авторизация пользователя")
    void  whenSignInValidCredentialsThenReturnsJwtResponse() {
        when(userService.userDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername(signInRequest.getUsername()))
                .thenReturn(testUser);
        when(jwtService.generateToken(testUser)).thenReturn(testJwtToken);

        JwtAuthenticationResponse response = authenticationService.signIn(signInRequest);

        assertNotNull(response);
        assertEquals(testJwtToken, response.getToken());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getUsername(),
                        signInRequest.getPassword()
                )
        );
        verify(userDetailsService).loadUserByUsername(signInRequest.getUsername());
        verify(jwtService).generateToken(testUser);
    }

    @Test
    @DisplayName("При пустом запросе бросает исключение")
    void signInWishNullRequestThenThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            authenticationService.signIn(null);
        });
    }

    @Test
    @DisplayName("Попытка передачи некорректных для авторизации данных")
    void whenSignInInvalidCredentialsThenThrowsBadCredentialsException() {
        SignInRequest invalidRequest = SignInRequest.builder()
                .username("wrongUser")
                .password("wrongPassword")
                .build();

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.signIn(invalidRequest);
        });

        verify(jwtService, never()).generateToken(any());
    }
}
