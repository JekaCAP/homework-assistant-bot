package ru.assistant.bot.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GitHubService
 * @author agent
 * @since 03.02.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHub gitHub;
    private final GitHubProperties properties;

    private static final Pattern PR_URL_PATTERN = Pattern.compile(
            "https://github\\.com/([^/]+)/([^/]+)/pull/(\\d+)"
    );

    private static final Pattern REPO_PATTERN = Pattern.compile(
            "github\\.com/([^/]+)/([^/]+)"
    );

    public boolean isValidPullRequestUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return PR_URL_PATTERN.matcher(url.trim()).matches();
    }

    @Cacheable(value = "prInfo", unless = "#result == null")
    public PullRequestInfo getPullRequestInfo(String prUrl) {
        log.info("🔍 Fetching PR info: {}", prUrl);

        Matcher matcher = PR_URL_PATTERN.matcher(prUrl);
        if (!matcher.find()) {
            log.error("Invalid PR URL format: {}", prUrl);
            throw new IllegalArgumentException("Неверный формат ссылки на Pull Request: " + prUrl);
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        int prNumber = Integer.parseInt(matcher.group(3));
        String repoFullName = owner + "/" + repo;

        log.debug("Processing: {}/{} #{}", owner, repo, prNumber);

        try {
            // Получаем информацию о репозитории с логированием
            log.debug("Getting repository: {}", repoFullName);
            GHRepository repository = gitHub.getRepository(repoFullName);
            log.debug("Repository found: {} (private: {})",
                    repository.getFullName(), repository.isPrivate());

            // Получаем информацию о PR
            log.debug("Getting PR #{}", prNumber);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);

            log.info("✅ PR retrieved: #{}-{} (state: {})",
                    pullRequest.getNumber(), pullRequest.getTitle(), pullRequest.getState());

            return PullRequestInfo.builder()
                    .url(prUrl)
                    .repository(repoFullName)
                    .number(prNumber)
                    .title(pullRequest.getTitle())
                    .author(pullRequest.getUser().getLogin())
                    .state(pullRequest.getState().name())
                    .isOpen(pullRequest.getState().name().equalsIgnoreCase("open"))
                    .createdAt(pullRequest.getCreatedAt())
                    .updatedAt(pullRequest.getUpdatedAt())
                    .headSha(pullRequest.getHead().getSha())
                    .mergeable(pullRequest.getMergeable())
                    .commits(pullRequest.getCommits())
                    .additions(pullRequest.getAdditions())
                    .deletions(pullRequest.getDeletions())
                    .changedFiles(pullRequest.getChangedFiles())
                    .build();

        } catch (IOException e) {
            handleGitHubException(e, repoFullName, prNumber, prUrl);
            // Эта строка никогда не выполнится, но нужна для компилятора
            throw new GitHubApiException("Failed to fetch PR info", e);
        }
    }

    private void handleGitHubException(IOException e, String repoFullName, int prNumber, String prUrl) {
        String errorMessage = e.getMessage();

        if (errorMessage == null) {
            log.error("Unknown error for {}/{}: {}", repoFullName, prNumber, e.getClass().getName(), e);
            throw new GitHubApiException("Неизвестная ошибка при обращении к GitHub API");
        }

        if (errorMessage.contains("Not Found")) {
            log.error("Repository or PR not found: {}/{}", repoFullName, prNumber);
            throw new ResourceNotFoundException(
                    String.format("Репозиторий или Pull Request не найден: %s #%d", repoFullName, prNumber));
        }

        if (errorMessage.contains("rate limit")) {
            log.error("GitHub API rate limit exceeded");
            throw new RateLimitException("Превышен лимит запросов GitHub API. Попробуйте позже.");
        }

        if (errorMessage.contains("timed out") || errorMessage.contains("timeout") ||
            errorMessage.contains("HTTP response code: -1")) {
            log.error("GitHub API timeout for {}: {}", prUrl, errorMessage);
            throw new GitHubApiException(
                    String.format("Таймаут при обращении к GitHub API. Попробуйте снова через несколько секунд. " +
                                  "Текущие таймауты: connect=%dms, read=%dms",
                            properties.getConnectTimeout(), properties.getReadTimeout()));
        }

        log.error("GitHub API error for {}: {}", prUrl, errorMessage, e);
        throw new GitHubApiException("Ошибка GitHub API: " + errorMessage, e);
    }

    public boolean repositoryExists(String repoUrl) {
        Matcher matcher = REPO_PATTERN.matcher(repoUrl);
        if (!matcher.find()) {
            return false;
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        String repoFullName = owner + "/" + repo;

        try {
            gitHub.getRepository(repoFullName);
            return true;
        } catch (IOException e) {
            log.debug("Repository not found: {}", repoFullName);
            return false;
        }
    }

    public RepositoryAccessInfo checkRepositoryAccess(String repoUrl) {
        Matcher matcher = REPO_PATTERN.matcher(repoUrl);
        if (!matcher.find()) {
            return RepositoryAccessInfo.builder()
                    .exists(false)
                    .error("Invalid repository URL format")
                    .build();
        }

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        String repoFullName = owner + "/" + repo;

        try {
            GHRepository repository = gitHub.getRepository(repoFullName);

            return RepositoryAccessInfo.builder()
                    .exists(true)
                    .fullName(repository.getFullName())
                    .owner(repository.getOwnerName())
                    .isPrivate(repository.isPrivate())
                    .description(repository.getDescription())
                    .url(repository.getHtmlUrl().toString())
                    .hasAccess(!repository.isPrivate() || repository.hasPullAccess())
                    .build();

        } catch (IOException e) {
            return RepositoryAccessInfo.builder()
                    .exists(false)
                    .fullName(repoFullName)
                    .error(e.getMessage())
                    .build();
        }
    }

    public boolean isAuthenticated() {
        return !gitHub.isAnonymous();
    }

    public String getAuthenticatedUser() {
        try {
            return gitHub.getMyself().getLogin();
        } catch (IOException e) {
            log.warn("Failed to get authenticated user", e);
            return null;
        }
    }

    // === DTO Classes ===

    @lombok.Builder
    @lombok.Data
    public static class PullRequestInfo {
        private String url;
        private String repository;
        private Integer number;
        private String title;
        private String author;
        private String state;
        private Boolean isOpen;
        private java.util.Date createdAt;
        private java.util.Date updatedAt;
        private String headSha;
        private Boolean mergeable;
        private Integer commits;
        private Integer additions;
        private Integer deletions;
        private Integer changedFiles;
    }

    @lombok.Builder
    @lombok.Data
    public static class RepositoryAccessInfo {
        private boolean exists;
        private String fullName;
        private String owner;
        private Boolean isPrivate;
        private String description;
        private String url;
        private boolean hasAccess;
        private String error;
    }

    // === Exception Classes ===

    public static class GitHubApiException extends RuntimeException {
        public GitHubApiException(String message) {
            super(message);
        }
        public GitHubApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ResourceNotFoundException extends GitHubApiException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class RateLimitException extends GitHubApiException {
        public RateLimitException(String message) {
            super(message);
        }
    }
}