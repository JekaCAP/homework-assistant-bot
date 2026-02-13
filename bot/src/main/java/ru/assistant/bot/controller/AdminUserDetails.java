package ru.assistant.bot.controller;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.assistant.bot.model.Admin;

import java.util.Collection;
import java.util.List;

/**
 * AdminUserDetails — описание класса.
 * <p>
 * TODO: добавить описание назначения и поведения класса.
 * </p>
 *
 * @author agent
 * @since 13.02.2026
 */
@Getter
public class AdminUserDetails implements UserDetails {

    private final Admin admin;

    public AdminUserDetails(Admin admin) {
        this.admin = admin;
    }

    public String getFullName() {
        return admin.getFullName() != null ? admin.getFullName() : admin.getTelegramUsername();
    }

    public String getTelegramUsername() {
        return admin.getTelegramUsername();
    }

    public Long getTelegramId() {
        return admin.getTelegramId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ВАЖНО: роль должна быть ROLE_ADMIN
        String role = "ROLE_" + admin.getRole().name();
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return admin.getPasswordHash() != null ? admin.getPasswordHash() : "";
    }

    @Override
    public String getUsername() {
        return admin.getTelegramUsername() != null ?
                admin.getTelegramUsername() : admin.getTelegramId().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return admin.getIsActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return admin.getIsActive() && admin.getWebEnabled();
    }
}