package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * AssignmentUpdateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class AssignmentUpdateRequest {
    @Size(min = 3, max = 300)
    private String title;

    @Size(max = 5000)
    private String description;

    @Min(1)
    private Integer number;

    private Set<String> requirements;

    @Min(0)
    @Max(1000)
    private Integer maxScore;

    @Min(0)
    @Max(100)
    private Integer minScore;

    private LocalDateTime deadline;

    @Pattern(
            regexp = "^https://github\\.com/[^/]+/[^/]+/pull/\\d+$",
            message = "URL должен быть валидной ссылкой на Pull Request GitHub"
    )
    private String githubTemplateUrl;

    @Size(max = 200)
    private String testCommand;

    @Min(1)
    @Max(100)
    private Integer estimatedHours;

    private String type;

    private Boolean isActive;
}