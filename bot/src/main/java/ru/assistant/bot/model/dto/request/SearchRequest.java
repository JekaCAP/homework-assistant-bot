package ru.assistant.bot.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * SearchRequest
 * @author agent
 * @since 03.02.2026
 */
@Data
public class SearchRequest {
    private String query;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;

    private String sortBy;

    private Boolean sortDesc = false;
}