package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.ExampleController;
import com.example.bankcards.security.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class ExampleControllerImpl implements ExampleController {
    private final UserService service;

    @Override
    public String example() {
        return "Hello, world!";
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String exampleAdmin() {
        return "Hello, admin!";
    }
}
