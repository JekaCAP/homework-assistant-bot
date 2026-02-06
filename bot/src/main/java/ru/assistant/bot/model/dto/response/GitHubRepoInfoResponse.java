package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * GitHubRepoInfoResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepoInfoResponse {
    private String fullName;
    private String description;
    private String language;
    private Integer stars;
    private Integer forks;
    private Integer openIssues;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String defaultBranch;
}