package ru.assistant.bot.github;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "github")
public class GitHubProperties {


    @NotBlank
    private String token;

    private String apiUrl = "https://api.github.com";
    private int connectTimeout = 10000;
    private int readTimeout = 30000;
    private int maxRetries = 3;
    private long retryDelayMs = 1000;
}