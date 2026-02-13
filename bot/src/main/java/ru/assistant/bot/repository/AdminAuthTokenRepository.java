package ru.assistant.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.assistant.bot.model.Admin;
import ru.assistant.bot.model.AdminAuthToken;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * AdminAuthTokenRepository — описание интерфейса.
 * <p>
 * TODO: описать, какие обязанности реализует интерфейс.
 * </p>
 *
 * @author agent
 * @since 12.02.2026
 */
public interface AdminAuthTokenRepository extends JpaRepository<AdminAuthToken, Long> {

    Optional<AdminAuthToken> findByToken(String token);

    void deleteByAdmin(Admin admin);

    @Modifying
    @Transactional
    @Query("DELETE FROM AdminAuthToken t WHERE t.expiresAt < :now OR t.usedAt IS NOT NULL")
    int deleteAllExpiredOrUsed(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AdminAuthToken t WHERE t.admin.id = :adminId AND (t.usedAt IS NOT NULL OR t.expiresAt < :now)")
    int deleteExpiredOrUsedByAdmin(Long adminId, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AdminAuthToken t WHERE t.admin.id = :adminId AND t.expiresAt < :now")
    int deleteExpiredByAdmin( Long adminId, LocalDateTime now);
}