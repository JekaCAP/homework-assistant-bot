package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CourseResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String difficultyLevel;
    private Boolean isActive;
    private Integer sortOrder;
    private Integer totalAssignments;
    private Integer activeStudents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}