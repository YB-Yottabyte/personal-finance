package com.finance.manager.ui;

import com.finance.manager.MainApp;
import com.finance.manager.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login window shown on startup.
 * Each tab (Login / Register) has its own independent field instances —
 * a Swing component can only belong to one parent at a time.
 */
@Component
public class LoginFrame extends JFrame {

    private final UserService        userService;
    private final ApplicationContext ctx;
    private final JLabel             statusLabel = new JLabel(" ");

    // Login tab fields
    private final JTextField     loginUser = new JTextField(18);
    private final JPasswordField loginPass = new JPasswordField(18);

    // Register tab fields
    private final JTextField     regUser = new JTextField(18);
    private final JPasswordField regPass = new JPasswordField(18);

    public LoginFrame(UserService userService, ApplicationContext ctx) {
        super("Personal Finance Manager — Login");
        this.userService = userService;
        this.ctx         = ctx;
        buildUI();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(28, 44, 24, 44));

        JLabel title = new JLabel("Personal Finance Manager", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        root.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login",    buildLoginPanel());
        tabs.addTab("Register", buildRegisterPanel());
        tabs.addChangeListener(e -> statusLabel.setText(" "));
        root.add(tabs, BorderLayout.CENTER);

        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(statusLabel, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(16, 0, 10, 0));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 10));
        form.add(new JLabel("Username:")); form.add(loginUser);
        form.add(new JLabel("Password:")); form.add(loginPass);
        panel.add(form, BorderLayout.CENTER);

        JButton btn = new JButton("Login");
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.addActionListener(e -> handleLogin());
        loginPass.addActionListener(e -> handleLogin());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.add(btn);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(16, 0, 10, 0));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 10));
        form.add(new JLabel("Username:")); form.add(regUser);
        form.add(new JLabel("Password:")); form.add(regPass);
        panel.add(form, BorderLayout.CENTER);

        JButton btn = new JButton("Create Account");
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.addActionListener(e -> handleRegister());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.add(btn);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    private void handleLogin() {
        String username = loginUser.getText().trim();
        String password = new String(loginPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Username and password are required.", Color.RED);
            return;
        }
        if (userService.authenticate(username, password)) {
            dispose();
            SwingUtilities.invokeLater(() -> ctx.getBean(MainApp.class).show());
        } else {
            setStatus("Invalid username or password.", Color.RED);
            loginPass.setText("");
        }
    }

    private void handleRegister() {
        String username = regUser.getText().trim();
        String password = new String(regPass.getPassword());

        try {
            userService.register(username, password);
            setStatus("Account created — you can now log in.", new Color(0, 130, 0));
            regUser.setText("");
            regPass.setText("");
        } catch (IllegalArgumentException ex) {
            setStatus(ex.getMessage(), Color.RED);
        }
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
