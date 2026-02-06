package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * AdminUpdateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class AdminUpdateRequest {
    @Size(min = 2, max = 200)
    private String fullName;

    @Email
    private String email;

    private String role;

    private Set<String> permissions;

    private Boolean isActive;
}