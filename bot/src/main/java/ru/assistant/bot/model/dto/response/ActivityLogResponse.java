package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ActivityLogResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private String userType;
    private String userTypeDisplayName;
    private Long userId;
    private String action;
    private String description;
    private String details;
    private String ipAddress;
    private String userAgent;
    private Long telegramChatId;
    private Boolean success;
    private String errorMessage;
    private Long executionTimeMs;
    private LocalDateTime createdAt;
}