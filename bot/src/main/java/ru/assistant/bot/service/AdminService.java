package ru.assistant.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.assistant.bot.model.Admin;
import ru.assistant.bot.repository.AdminRepository;

import java.util.List;
import java.util.Optional;

/**
 * AdminService
 * @author agent
 * @since 03.02.2026
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public List<Admin> getActiveAdmins() {
        return adminRepository.findByIsActiveTrue();
    }

    public boolean isAdmin(Long telegramId) {
        return adminRepository.findByTelegramId(telegramId)
                .map(Admin::getIsActive)
                .orElse(false);
    }

    public boolean existsByTelegramId(Long telegramId) {
        return adminRepository.findByTelegramIdAndIsActiveTrue(telegramId).isPresent();
    }

    public Optional<Admin> findByTelegramId(Long telegramId) {
        return adminRepository.findByTelegramId(telegramId);
    }
}