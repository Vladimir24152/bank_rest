package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.request.SignInRequest;
import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.dto.response.JwtAuthenticationResponse;
import com.example.bankcards.service.AuthenticationService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthControllerImpl implements AuthController {

    private final AuthenticationService authenticationService;

    private final UserService service;

    @Override
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @Override
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void giveAdmin(Long userId) {
        service.giveAdmin(userId);
    }
}
