package ui;

import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardPanel extends BasePanel {

    public DashboardPanel(AppWindow app) {
        super(app, app.isAdmin() ? "Admin Panel" : "Customer Portal", null);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(new Color(18, 22, 35));
        contentArea.add(centre, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout(0, 24));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(500, app.isAdmin() ? 340 : 420));

        // Welcome header
        JPanel welcomeBox = new JPanel(new GridLayout(2,1));
        welcomeBox.setBackground(new Color(24, 30, 46));
        welcomeBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(app.isAdmin() ? new Color(0,180,160) : new Color(120,80,220), 2),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));
        JLabel welcome = new JLabel(app.isAdmin() ? "🔐  Welcome, Admin!" : "👤  Welcome, Customer!", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcome.setForeground(app.isAdmin() ? new Color(0,220,200) : new Color(160,130,255));
        JLabel sub = new JLabel(app.isAdmin() ? "Manage cars and customers" : "Browse, register and rent cars", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(120,140,170));
        welcomeBox.add(welcome); welcomeBox.add(sub);
        wrapper.add(welcomeBox, BorderLayout.NORTH);

        // Buttons
        JPanel btnGrid;
        if (app.isAdmin()) {
            btnGrid = new JPanel(new GridLayout(2, 2, 16, 16));
            btnGrid.setOpaque(false);
            btnGrid.add(dashBtn("➕  Add Car",        new Color(0,140,120),   e -> app.showPanel(AppWindow.ADD_CAR)));
            btnGrid.add(dashBtn("🔍  View Cars",       new Color(30,90,170),   e -> app.showPanel(AppWindow.VIEW_CARS)));
            btnGrid.add(dashBtn("👥  View Customers",  new Color(100,60,180),  e -> app.showPanel(AppWindow.VIEW_CUSTOMERS)));
            btnGrid.add(dashBtn("🚪  Exit",            new Color(80,40,40),    e -> {
                int ok = JOptionPane.showConfirmDialog(app, "Exit the application?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) { DBConnection.closeConnection(); System.exit(0); }
            }));
        } else {
            btnGrid = new JPanel(new GridLayout(4, 1, 0, 16));
            btnGrid.setOpaque(false);
            btnGrid.add(dashBtn("🔍  View Available Cars",  new Color(30,90,170),   e -> app.showPanel(AppWindow.VIEW_CARS)));
            btnGrid.add(dashBtn("👤  Register as Customer", new Color(100,60,180),  e -> app.showPanel(AppWindow.REGISTER_CUSTOMER)));
            btnGrid.add(dashBtn("🚘  Rent a Car",           new Color(180,110,0),   e -> app.showPanel(AppWindow.RENT_CAR)));
            btnGrid.add(dashBtn("↩️  Return a Car",         new Color(160,40,40),   e -> app.showPanel(AppWindow.RETURN_CAR)));
        }
        wrapper.add(btnGrid, BorderLayout.CENTER);
        centre.add(wrapper);
    }

    private JButton dashBtn(String text, Color bg, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }
}
