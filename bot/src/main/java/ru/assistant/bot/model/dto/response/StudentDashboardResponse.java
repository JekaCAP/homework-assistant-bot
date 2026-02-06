package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * StudentDashboardResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private StudentResponse student;
    private List<CourseProgressResponse> coursesProgress;
    private List<RecentSubmissionResponse> recentSubmissions;
    private DashboardStats stats;
    private List<UpcomingAssignmentResponse> upcomingAssignments;
}