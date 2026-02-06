package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DateRangeRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class DateRangeRequest {
    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;
}