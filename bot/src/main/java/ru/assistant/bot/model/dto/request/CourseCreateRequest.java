package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * CourseCreateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class CourseCreateRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String code;

    @NotBlank
    @Size(min = 3, max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    private String icon;

    private String difficultyLevel;

    @Min(0)
    private Integer sortOrder;
}