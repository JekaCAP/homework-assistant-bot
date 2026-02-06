package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * StudentResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private Long telegramId;
    private String telegramUsername;
    private String fullName;
    private String githubUsername;
    private String email;
    private Boolean isActive;
    private LocalDateTime registrationDate;
    private LocalDateTime lastActivity;
    private String settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}