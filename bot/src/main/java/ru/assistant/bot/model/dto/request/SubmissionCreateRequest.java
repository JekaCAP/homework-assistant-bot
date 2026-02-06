package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SubmissionCreateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class SubmissionCreateRequest {
    @NotNull
    private Long studentTelegramId;

    @NotNull
    private Long assignmentId;

    @NotBlank
    @Pattern(
            regexp = "^https://github\\.com/[^/]+/[^/]+/pull/\\d+$",
            message = "URL должен быть валидной ссылкой на Pull Request GitHub"
    )
    private String prUrl;

    @Size(max = 1000)
    private String studentComment;
}