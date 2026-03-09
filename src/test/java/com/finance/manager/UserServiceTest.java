package com.finance.manager;

import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.finance.manager.config.SecurityConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests {@link UserService} using the JPA slice + a real BCryptPasswordEncoder.
 * {@code @Import(SecurityConfig.class)} provides the {@link PasswordEncoder} bean.
 */
@DataJpaTest
@Import({UserService.class, SecurityConfig.class})
class UserServiceTest {

    @Autowired private UserService    userService;
    @Autowired private UserRepository userRepository;

    static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(4);

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void register_savesUserWithHashedPassword() {
        User user = userService.register("alice", "secret123");

        assertThat(user.getId()).isPositive();
        assertThat(user.getUsername()).isEqualTo("alice");
        // Password must NOT be stored as plain text
        assertThat(user.getPasswordHash()).isNotEqualTo("secret123");
        assertThat(ENCODER.matches("secret123", user.getPasswordHash())).isTrue();
    }

    @Test
    void register_throwsWhenUsernameTaken() {
        userService.register("bob", "pass123");
        assertThatThrownBy(() -> userService.register("bob", "other123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void register_throwsOnBlankUsername() {
        assertThatThrownBy(() -> userService.register("  ", "pass123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void register_throwsWhenPasswordTooShort() {
        assertThatThrownBy(() -> userService.register("carol", "12345"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("6 characters");
    }

    @Test
    void authenticate_returnsTrueForCorrectCredentials() {
        userService.register("dave", "mypassword");
        assertThat(userService.authenticate("dave", "mypassword")).isTrue();
    }

    @Test
    void authenticate_returnsFalseForWrongPassword() {
        userService.register("eve", "correct");
        assertThat(userService.authenticate("eve", "wrong")).isFalse();
    }

    @Test
    void authenticate_returnsFalseForUnknownUser() {
        assertThat(userService.authenticate("nobody", "pass")).isFalse();
    }

    @Test
    void hasAnyUser_falseWhenEmpty() {
        assertThat(userService.hasAnyUser()).isFalse();
    }

    @Test
    void hasAnyUser_trueAfterRegistration() {
        userService.register("frank", "pass123");
        assertThat(userService.hasAnyUser()).isTrue();
    }
}
