package ru.assistant.bot.github;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GitHubConfig {

    private final GitHubProperties properties;

    @Bean
    public GitHub gitHub() throws IOException {
        String token = properties.getToken();

        if (token == null || token.trim().isEmpty()) {
            log.warn("⚠️ GitHub token is not configured. Using anonymous access (limited)");
            return GitHub.connectAnonymously();
        }

        try {
            GitHub github = GitHub.connectUsingOAuth(token);

            log.info("✅ GitHub client configured successfully with token");
            log.info("Connected as: {}", github.getMyself().getLogin());

            return github;

        } catch (IOException e) {
            log.error("Failed to create GitHub client with token, falling back to anonymous", e);
            return GitHub.connectAnonymously();
        }
    }
}