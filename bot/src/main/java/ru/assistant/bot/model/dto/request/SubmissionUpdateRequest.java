package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SubmissionUpdateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class SubmissionUpdateRequest {

    @Pattern(
            regexp = "^https://github\\.com/[^/]+/[^/]+/pull/\\d+$",
            message = "URL должен быть валидной ссылкой на Pull Request GitHub"
    )
    private String prUrl;

    @Size(max = 1000)
    private String studentComment;
}