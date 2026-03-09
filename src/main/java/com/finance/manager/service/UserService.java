package com.finance.manager.service;

import com.finance.manager.User;
import com.finance.manager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and authentication.
 *
 * <p>Passwords are hashed with BCrypt before storage — the plain-text
 * password is never persisted. {@link PasswordEncoder#matches} does a
 * constant-time comparison to prevent timing attacks.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user. Throws if the username is already taken.
     *
     * @param username  desired username
     * @param rawPassword plain-text password (hashed before storage)
     */
    public User register(String username, String rawPassword) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be blank.");
        if (rawPassword == null || rawPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken.");

        String hash = passwordEncoder.encode(rawPassword);
        return userRepository.save(new User(username, hash));
    }

    /**
     * Returns {@code true} if the username exists and the raw password
     * matches the stored BCrypt hash.
     */
    @Transactional(readOnly = true)
    public boolean authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyUser() {
        return userRepository.count() > 0;
    }
}
