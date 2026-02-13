package ru.assistant.bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.assistant.bot.model.Admin;
import ru.assistant.bot.model.AdminAuthToken;
import ru.assistant.bot.repository.AdminAuthTokenRepository;
import ru.assistant.bot.repository.AdminRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * AdminWebAuthService
 * @author agent
 * @since 12.02.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminWebAuthService {

    private final AdminRepository adminRepository;
    private final AdminAuthTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRE_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Создать одноразовый токен для входа через Telegram
     */
    @Transactional
    public String generateTelegramLoginToken(Long telegramId) {
        Optional<Admin> adminOpt = adminRepository.findByTelegramIdAndIsActiveTrue(telegramId);

        if (adminOpt.isEmpty()) {
            log.warn("Попытка генерации токена несуществующим админом: {}", telegramId);
            return null;
        }

        Admin admin = adminOpt.get();

        tokenRepository.deleteExpiredByAdmin(admin.getId(), LocalDateTime.now());

        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_EXPIRE_MINUTES);

        AdminAuthToken authToken = AdminAuthToken.builder()
                .token(token)
                .admin(admin)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(authToken);

        log.info("Сгенерирован токен для админа: {} ({})", admin.getTelegramUsername(), telegramId);
        return token;
    }

    /**
     * Валидация токена и получение админа
     */
    @Transactional
    public Optional<Admin> validateAndConsumeToken(String token) {
        Optional<AdminAuthToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            log.warn("Попытка входа с несуществующим токеном");
            return Optional.empty();
        }

        AdminAuthToken authToken = tokenOpt.get();

        if (authToken.isExpired()) {
            log.warn("Просроченный токен: {}", token);
            tokenRepository.delete(authToken);
            return Optional.empty();
        }

        if (authToken.isUsed()) {
            log.warn("Уже использованный токен: {}", token);
            tokenRepository.delete(authToken);
            return Optional.empty();
        }

        Admin admin = authToken.getAdmin();

        if (!admin.getIsActive() || !admin.getWebEnabled()) {
            log.warn("Админ неактивен или не имеет доступа к веб: {}", admin.getTelegramId());
            tokenRepository.delete(authToken);
            return Optional.empty();
        }

        authToken.markAsUsed();
        tokenRepository.save(authToken);

        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);

        log.info("Успешный вход через токен: {}", admin.getTelegramUsername());
        return Optional.of(admin);
    }

    /**
     * Установка пароля для веб-доступа
     */
    @Transactional
    public boolean setupPassword(Long adminId, String rawPassword) {
        Optional<Admin> adminOpt = adminRepository.findById(adminId);

        if (adminOpt.isEmpty()) {
            return false;
        }

        Admin admin = adminOpt.get();
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setWebEnabled(true);
        adminRepository.save(admin);

        log.info("Установлен пароль для админа: {}", admin.getTelegramUsername());
        return true;
    }

    /**
     * Проверка пароля при логине
     */
    public boolean checkPassword(Admin admin, String rawPassword) {
        if (admin.getPasswordHash() == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, admin.getPasswordHash());
    }

    /**
     * Генерация безопасного токена
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Очистка старых токенов
     */
    @Transactional
    public int cleanExpiredTokens() {
        return tokenRepository.deleteAllExpiredOrUsed(LocalDateTime.now());
    }
}