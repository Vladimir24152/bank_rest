package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.bankcards.constant.ApiConstant.CARD_BASE_URL;
import static com.example.bankcards.constant.ApiConstant.CREATE;

@RestController
@RequestMapping(CARD_BASE_URL)
@RequiredArgsConstructor
public class CardControllerImpl implements CardController {

    private final CardService cardService;

    @Override
    @PostMapping(CREATE)
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest createCardRequest) {

        System.out.println("=== DEBUG ===");
        System.out.println("Запрос получен: " + createCardRequest);
        System.out.println("CardNumber: " + createCardRequest.getCardNumber());
        System.out.println("ClientId: " + createCardRequest.getClientId());

        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(createCardRequest));
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authentication != null);
        response.put("name", authentication != null ? authentication.getName() : null);
        response.put("authorities", authentication != null ?
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()) : null);
        return ResponseEntity.ok(response);
    }
}
