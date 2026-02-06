package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * AssignmentResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private Long courseId;
    private String courseName;
    private Integer number;
    private String title;
    private String description;
    private Set<String> requirements;
    private Integer maxScore;
    private Integer minScore;
    private LocalDateTime deadline;
    private Boolean isActive;
    private String githubTemplateUrl;
    private String testCommand;
    private Integer estimatedHours;
    private String type;
    private Boolean isPastDeadline;
    private Integer totalSubmissions;
    private Double averageScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}