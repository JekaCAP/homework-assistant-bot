package ru.assistant.bot.model;

/**
 * UserType
 * @author agent
 * @since 03.02.2026
 */
public enum UserType {
    STUDENT("Студент"),
    ADMIN("Администратор"),
    SYSTEM("Система"),
    BOT("Бот");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}