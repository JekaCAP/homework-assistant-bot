package ru.assistant.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.assistant.bot.model.Admin;
import ru.assistant.bot.model.enums.AdminRole;

import java.util.List;
import java.util.Optional;

/**
 * AdminRepository
 * @author agent
 * @since 03.02.2026
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByTelegramId(Long telegramId);

    List<Admin> findByIsActiveTrue();

    Optional<Admin> findByTelegramIdAndIsActiveTrue(Long telegramId);

    Optional<Admin> findByTelegramUsername(String telegramUsername);
}