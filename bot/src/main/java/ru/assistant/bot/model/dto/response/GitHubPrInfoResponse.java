package ru.assistant.bot.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * GitHubPrInfoResponse
 * @author agent
 * @since 03.02.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubPrInfoResponse {
    private String url;
    private String repository;
    private Integer number;
    private String title;
    private String author;
    private String state;
    private Boolean isOpen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastCommit;
    private Integer commits;
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private Boolean mergeable;
    private String mergeableState;
}