package com.example.bankcards.controller;

import com.example.bankcards.dto.request.SignInRequest;
import com.example.bankcards.dto.request.SignUpRequest;
import com.example.bankcards.dto.response.JwtAuthenticationResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.example.bankcards.constant.ApiConstant.AUTH_BASE_URL;
import static com.example.bankcards.constant.ApiConstant.GIVE_ADMIN;
import static com.example.bankcards.constant.ApiConstant.SIGN_IN;
import static com.example.bankcards.constant.ApiConstant.SIGN_UP;
import static com.example.bankcards.constant.ApiConstant.USER_ID;

@RequestMapping(AUTH_BASE_URL)
public interface AuthController {

    @Operation(summary = "Регистрация пользователя")
    @PostMapping(SIGN_UP)
    JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request);

    @Operation(summary = "Авторизация пользователя")
    @PostMapping(SIGN_IN)
    JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request);

    @PostMapping(GIVE_ADMIN + USER_ID)
    @Operation(summary = "Назначить роль ADMIN для зарегистрированного пользователя")
    void giveAdmin(@Positive @PathVariable("userId") Long userId);
}
