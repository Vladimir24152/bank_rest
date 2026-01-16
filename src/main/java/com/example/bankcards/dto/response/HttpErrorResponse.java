package com.example.bankcards.dto.response;

public record HttpErrorResponse(int code, String type, String message) {
}
