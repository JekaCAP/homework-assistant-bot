package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TelegramButtonResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramButtonResponse {
    private String text;
    private String callbackData;
    private String url;
    private Boolean requestContact;
    private Boolean requestLocation;
}