package ru.assistant.bot.model.enums;

/**
 * SubmissionStatus
 * @author agent
 * @since 03.02.2026
 */
public enum SubmissionStatus {
    SUBMITTED("Сдано"),
    UNDER_REVIEW("На проверке"),
    NEEDS_REVISION("Требует доработки"),
    ACCEPTED("Принято"),
    REJECTED("Отклонено");

    private final String displayName;

    SubmissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}