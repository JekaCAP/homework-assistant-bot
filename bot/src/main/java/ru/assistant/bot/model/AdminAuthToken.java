package ru.assistant.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Одноразовый токен для входа в веб-админку через Telegram
 *
 * @author agent
 * @since 12.02.2026
 */
@Entity
@Table(name = "admin_auth_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin_auth_token_seq")
    @SequenceGenerator(name = "admin_auth_token_seq",
            sequenceName = "admin_auth_token_seq",
            allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Проверка, истёк ли токен
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Проверка, использован ли токен
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Пометить токен как использованный
     */
    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }
}