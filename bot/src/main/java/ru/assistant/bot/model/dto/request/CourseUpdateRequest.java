package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * CourseUpdateRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class CourseUpdateRequest {
    @Size(min = 3, max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    private String icon;

    private String difficultyLevel;

    @Min(0)
    private Integer sortOrder;

    private Boolean isActive;
}