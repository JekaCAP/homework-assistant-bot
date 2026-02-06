package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * CourseStats
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStats {
    private CourseResponse course;
    private Long totalStudents;
    private Long activeStudents;
    private Long totalSubmissions;
    private Long pendingSubmissions;
    private Double averageScore;
    private BigDecimal completionRate;
}