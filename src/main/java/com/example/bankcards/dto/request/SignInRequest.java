package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на аутентификацию")
public class SignInRequest {
    @Schema(description = "Имя пользователя", example = "Jon")
    @Size(min = 2, max = 100, message = "Имя пользователя должно содержать от 5 до 100 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String username;

    @Schema(description = "Пароль", example = "password123321")
    @Size(min = 8,max = 255, message = "Длина пароля должна быть не более 255 и не менее 8 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;
}
