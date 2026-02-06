package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TelegramKeyboardResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramKeyboardResponse {
    private String type; // inline, reply, remove
    private List<List<TelegramButtonResponse>> buttons;
}