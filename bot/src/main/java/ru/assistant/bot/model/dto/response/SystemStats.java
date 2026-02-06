package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SystemStats
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStats {
    private Long totalStudents;
    private Long activeStudents;
    private Long totalAdmins;
    private Long activeAdmins;
    private Long totalCourses;
    private Long activeCourses;
    private Long totalSubmissions;
    private Long pendingSubmissions;
    private Long submissionsToday;
    private Long reviewsToday;
}