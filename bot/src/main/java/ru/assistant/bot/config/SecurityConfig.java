package ru.assistant.bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.assistant.bot.controller.AdminUserDetails;
import ru.assistant.bot.model.Admin;
import ru.assistant.bot.repository.AdminRepository;

import java.util.Optional;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminRepository adminRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты - ВАЖНО: добавил /login!
                        .requestMatchers(
                                "/login",
                                "/login-error",
                                "/error/login-error",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        // Админка только для аутентифицированных
                        .requestMatchers("/admin/**").authenticated()
                        // Всё остальное требует аутентификации
                        .anyRequest().authenticated()
                )
                // Отключаем стандартную форму логина
                .formLogin(AbstractHttpConfigurer::disable)
                // Настройка выхода
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login-error")
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.info("🔐 Загрузка пользователя: {}", username);

            Optional<Admin> adminOpt = adminRepository.findByTelegramUsername(username);

            if (adminOpt.isEmpty()) {
                try {
                    Long telegramId = Long.parseLong(username);
                    adminOpt = adminRepository.findByTelegramIdAndIsActiveTrue(telegramId);
                } catch (NumberFormatException ignored) {
                }
            }

            Admin admin = adminOpt.orElseThrow(() ->
                    new UsernameNotFoundException("Админ не найден: " + username)
            );

            log.info("✅ Админ найден: {}, роль: {}, активен: {}, webEnabled: {}",
                    admin.getTelegramUsername(), admin.getRole(),
                    admin.getIsActive(), admin.getWebEnabled());

            if (!admin.getIsActive() || !admin.getWebEnabled()) {
                throw new UsernameNotFoundException("Админ неактивен или не имеет доступа к веб-интерфейсу");
            }

            AdminUserDetails userDetails = new AdminUserDetails(admin);
            log.info("🔑 Созданы authorities: {}", userDetails.getAuthorities());

            return userDetails;
        };
    }
}