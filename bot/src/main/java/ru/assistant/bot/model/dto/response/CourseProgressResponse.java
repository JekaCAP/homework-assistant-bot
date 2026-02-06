package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * CourseProgressResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressResponse {
    private CourseResponse course;
    private StudentProgressResponse progress;
    private List<AssignmentStatusResponse> assignmentsStatus;
    private Integer totalAssignments;
    private Integer completedAssignments;
    private BigDecimal overallProgress;
}