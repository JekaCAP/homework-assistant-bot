package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DashboardStats
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Integer totalCourses;
    private Integer activeCourses;
    private Integer totalAssignments;
    private Integer submittedAssignments;
    private Integer acceptedAssignments;
    private BigDecimal overallAverageScore;
    private Integer currentStreak;
    private Integer longestStreak;
}