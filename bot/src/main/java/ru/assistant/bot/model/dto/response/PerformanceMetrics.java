package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PerformanceMetrics
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    private Double averageReviewTimeHours;
    private Double submissionAcceptanceRate;
    private Double averageScore;
    private Long reviewsLastWeek;
    private Long reviewsLastMonth;
}