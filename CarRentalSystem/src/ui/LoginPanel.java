package ui;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends BasePanel {

    private static final String ADMIN_PASSWORD = "admin123";
    private final JComboBox<String> roleBox;
    private final JPasswordField    passwordField;

    public LoginPanel(AppWindow app) {
        super(app, "Login", null);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(new Color(18, 22, 35));
        contentArea.add(centre, BorderLayout.CENTER);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(24, 30, 46));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 180, 160), 2),
            BorderFactory.createEmptyBorder(50, 60, 50, 60)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        c.gridwidth = 2;

        c.gridx = 0; c.gridy = 0;
        JLabel title = new JLabel("🚗  Car Rental System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(0, 220, 200));
        card.add(title, c);

        c.gridy = 1;
        JLabel sub = new JLabel("Sign in to continue", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(new Color(100, 120, 150));
        card.add(sub, c);

        c.gridy = 2; card.add(Box.createVerticalStrut(10), c);

        c.gridwidth = 1; c.gridx = 0; c.gridy = 3; c.weightx = 0.3;
        card.add(fLabel("Login As:"), c);
        c.gridx = 1; c.weightx = 0.7;
        roleBox = new JComboBox<>(new String[]{"👤  Customer", "🔐  Admin"});
        styleCombo(roleBox);
        card.add(roleBox, c);

        c.gridx = 0; c.gridy = 4; c.weightx = 0.3;
        card.add(fLabel("Password:"), c);
        c.gridx = 1; c.weightx = 0.7;
        passwordField = new JPasswordField();
        stylePassField(passwordField);
        passwordField.setEnabled(false);
        card.add(passwordField, c);

        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        JLabel hint = new JLabel("  Password required for Admin only");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(80, 100, 130));
        card.add(hint, c);

        c.gridy = 6; c.insets = new Insets(20, 0, 0, 0);
        JButton loginBtn = makeBtn("Login  →", new Color(0, 140, 120));
        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        card.add(loginBtn, c);

        roleBox.addActionListener(e -> {
            boolean isAdmin = roleBox.getSelectedIndex() == 1;
            passwordField.setEnabled(isAdmin);
            passwordField.setBackground(isAdmin ? new Color(28, 34, 52) : new Color(14, 18, 28));
            if (!isAdmin) passwordField.setText("");
        });

        centre.add(card);
    }

    private void doLogin() {
        boolean isAdmin = roleBox.getSelectedIndex() == 1;
        if (isAdmin) {
            String pw = new String(passwordField.getPassword());
            if (!ADMIN_PASSWORD.equals(pw)) {
                JOptionPane.showMessageDialog(app, "Incorrect admin password!", "Access Denied", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                return;
            }
        }
        app.setAdmin(isAdmin);
        app.showPanel(AppWindow.DASHBOARD);
    }

    private JLabel fLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(new Color(170, 185, 210));
        return l;
    }
    private void styleCombo(JComboBox<String> c) {
        c.setBackground(new Color(28, 34, 52)); c.setForeground(Color.WHITE);
        c.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.setBorder(BorderFactory.createLineBorder(new Color(60, 80, 110)));
    }
    private void stylePassField(JPasswordField f) {
        f.setBackground(new Color(14, 18, 28)); f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE); f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 80, 110)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
    }
}
