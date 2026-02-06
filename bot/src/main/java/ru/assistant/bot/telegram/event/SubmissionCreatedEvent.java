package ru.assistant.bot.telegram.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import ru.assistant.bot.model.Submission;

/**
 * SubmissionCreatedEvent
 * @author agent
 * @since 03.02.2026
 */
@Getter
public class SubmissionCreatedEvent extends ApplicationEvent {
    private final Long submissionId;

    public SubmissionCreatedEvent(Long submissionId) {
        super(submissionId);
        this.submissionId = submissionId;
    }
}