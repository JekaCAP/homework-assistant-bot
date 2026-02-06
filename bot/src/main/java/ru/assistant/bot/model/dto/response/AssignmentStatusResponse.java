package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AssignmentStatusResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentStatusResponse {
    private AssignmentResponse assignment;
    private SubmissionResponse lastSubmission;
    private Boolean submitted;
    private Boolean accepted;
    private Integer attempts;
    private Integer bestScore;
    private LocalDateTime deadline;
    private Boolean isPastDeadline;
}