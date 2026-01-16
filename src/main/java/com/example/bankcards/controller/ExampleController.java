package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.example.bankcards.constant.ApiConstant.EXAMPLE_URL;

@RequestMapping(EXAMPLE_URL)
public interface ExampleController {

    @GetMapping
    @Operation(summary = "Доступен только авторизованным пользователям")
    String example();

    @GetMapping("/admin")
    @Operation(summary = "Доступен только авторизованным пользователям с ролью ADMIN")
    String exampleAdmin();
}
