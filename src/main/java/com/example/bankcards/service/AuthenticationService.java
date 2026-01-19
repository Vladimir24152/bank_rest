package com.example.bankcards.service;

import com.example.bankcards.dto.request.SignInRequest;
import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.dto.response.JwtAuthenticationResponse;

public interface AuthenticationService {

    JwtAuthenticationResponse signUp(SignUpRequest request);

    JwtAuthenticationResponse signIn(SignInRequest request);
}
