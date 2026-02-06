package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TelegramMessageResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageResponse {
    private Long messageId;
    private Long chatId;
    private String text;
    private LocalDateTime sentAt;
    private Boolean success;
    private String error;
}