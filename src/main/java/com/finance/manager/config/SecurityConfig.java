package com.finance.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides a BCrypt {@link PasswordEncoder} bean used by {@link com.finance.manager.service.UserService}.
 *
 * <p>Strength 12 means 2^12 = 4096 bcrypt rounds — production-grade cost
 * that makes brute-force attacks computationally infeasible.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
