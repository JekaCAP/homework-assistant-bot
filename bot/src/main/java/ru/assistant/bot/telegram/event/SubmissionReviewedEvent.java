package ru.assistant.bot.telegram.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import ru.assistant.bot.model.Submission;

/**
 * SubmissionReviewedEvent
 * @author agent
 * @since 03.02.2026
 */
@Getter
public class SubmissionReviewedEvent extends ApplicationEvent {
    private final Long submissionId;

    public SubmissionReviewedEvent(Long submissionId) {
        super(submissionId);
        this.submissionId = submissionId;
    }
}