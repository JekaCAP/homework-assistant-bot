package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UpcomingAssignmentResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingAssignmentResponse {
    private AssignmentResponse assignment;
    private CourseResponse course;
    private LocalDateTime deadline;
    private Long daysRemaining;
    private Boolean isSubmitted;
}