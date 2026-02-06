package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * AdminCreateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class AdminCreateRequest {
    @NotNull
    private Long telegramId;

    @Size(max = 100)
    private String telegramUsername;

    @NotBlank
    @Size(min = 2, max = 200)
    private String fullName;

    @Email
    private String email;

    private String role;

    private Set<String> permissions;
}