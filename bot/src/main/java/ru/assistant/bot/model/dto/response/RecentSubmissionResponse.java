package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RecentSubmissionResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentSubmissionResponse {
    private SubmissionResponse submission;
    private AssignmentResponse assignment;
    private CourseResponse course;
    private LocalDateTime submittedAt;
    private String status;
}