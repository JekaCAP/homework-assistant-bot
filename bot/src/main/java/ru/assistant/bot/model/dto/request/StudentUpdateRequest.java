package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * StudentUpdateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class StudentUpdateRequest {
    @Size(min = 2, max = 200)
    private String fullName;

    @Email
    private String email;

    @Pattern(regexp = "^[a-zA-Z\\d](?:[a-zA-Z\\d]|-(?=[a-zA-Z\\d])){0,38}$")
    private String githubUsername;

    private Boolean isActive;
}