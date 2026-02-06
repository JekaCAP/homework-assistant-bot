package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PendingReviewResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingReviewResponse {
    private SubmissionResponse submission;
    private StudentResponse student;
    private AssignmentResponse assignment;
    private LocalDateTime submittedAt;
    private Long waitingHours;
}