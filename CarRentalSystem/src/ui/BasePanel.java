package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Base panel with shared top navigation bar.
 * All panels extend this for consistent look and back/logout buttons.
 */
public abstract class BasePanel extends JPanel {

    protected final AppWindow app;
    protected final JPanel    contentArea;

    public BasePanel(AppWindow app, String title, String backPanel) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(new Color(18, 22, 35));
        setOpaque(true);

        // ── Top nav bar ────────────────────────────────────────────────────────
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(10, 13, 22));
        navBar.setPreferredSize(new Dimension(0, 52));
        navBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 180, 160)));

        JLabel appName = new JLabel("  🚗  Car Rental System");
        appName.setFont(new Font("SansSerif", Font.BOLD, 14));
        appName.setForeground(new Color(0, 200, 180));
        navBar.add(appName, BorderLayout.WEST);

        JLabel pageTitle = new JLabel(title, SwingConstants.CENTER);
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        pageTitle.setForeground(new Color(200, 215, 240));
        navBar.add(pageTitle, BorderLayout.CENTER);

        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightNav.setOpaque(false);

        if (backPanel != null) {
            JButton backBtn = navBtn("← Back", new Color(50, 60, 90));
            backBtn.addActionListener(e -> app.showPanel(backPanel));
            rightNav.add(backBtn);
        }

        if (!title.equals("Login")) {
            JButton logoutBtn = navBtn("Logout", new Color(120, 30, 30));
            logoutBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(app,
                    "Logout and return to login screen?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    app.forceShowLogin();
                }
            });
            rightNav.add(logoutBtn);
        }

        navBar.add(rightNav, BorderLayout.EAST);
        add(navBar, BorderLayout.NORTH);

        // ── Content area ───────────────────────────────────────────────────────
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(new Color(18, 22, 35));
        add(contentArea, BorderLayout.CENTER);
    }

    private JButton navBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    protected JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    protected JLabel makeLabel(String text, int size, boolean bold, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        lbl.setForeground(color);
        return lbl;
    }

    protected JTextField makeField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(28, 34, 52));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 80, 110)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return f;
    }
}
