package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SubmissionReviewRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class SubmissionReviewRequest {
    @NotNull
    @Min(0)
    @Max(100)
    private Integer score;

    @NotBlank
    @Size(min = 10, max = 2000)
    private String comment;

    @NotNull
    private Long reviewerId;
}