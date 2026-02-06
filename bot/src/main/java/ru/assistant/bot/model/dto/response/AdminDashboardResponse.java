package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AdminDashboardResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private SystemStats systemStats;
    private List<CourseStats> courseStats;
    private List<RecentActivityResponse> recentActivities;
    private List<PendingReviewResponse> pendingReviews;
    private PerformanceMetrics performanceMetrics;
}