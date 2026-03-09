package com.finance.manager;

import com.finance.manager.ui.LoginFrame;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

/**
 * Spring Boot entry point for the Personal Finance Manager.
 *
 * <p>Uses {@link WebApplicationType#NONE} so no embedded HTTP server starts —
 * this is a pure desktop app. After the Spring context is ready, the login
 * screen is shown on the EDT. On successful login, {@link MainApp} opens.
 */
@SpringBootApplication
public class FinanceManagerApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(FinanceManagerApplication.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);

        // Show login screen first — MainApp opens only after authentication
        SwingUtilities.invokeLater(() -> ctx.getBean(LoginFrame.class).setVisible(true));
    }
}
