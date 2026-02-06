package ru.assistant.bot.model.enums;

/**
 * UserState
 * @author agent
 * @since 03.02.2026
 */
public enum UserState {
    IDLE,
    WAITING_FOR_COURSE_SELECTION,
    WAITING_FOR_ASSIGNMENT_SELECTION,
    WAITING_FOR_PR_LINK,
    WAITING_FOR_GITHUB_USERNAME,
    VIEWING_PROGRESS
}