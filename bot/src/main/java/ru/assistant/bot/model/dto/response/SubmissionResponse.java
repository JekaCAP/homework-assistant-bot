package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SubmissionResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private Long id;
    private StudentResponse student;
    private AssignmentResponse assignment;
    private String prUrl;
    private Integer prNumber;
    private String githubRepo;
    private String commitHash;
    private String branchName;
    private String status;
    private String statusDisplayName;
    private String statusEmoji;
    private Integer score;
    private String reviewerComment;
    private String studentComment;
    private Boolean autoChecksPassed;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime resubmittedAt;
    private AdminResponse reviewer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}