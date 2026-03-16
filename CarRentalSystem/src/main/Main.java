package main;

import database.DBConnection;
import ui.AppWindow;
import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}

        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null || conn.isClosed()) throw new SQLException("Connection null.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to the database.\n\nPlease verify:\n  • MySQL server is running.\n  • schema.sql was executed.\n  • Password is correct in DBConnection.java.\n\nError: " + ex.getMessage(),
                "Database Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new AppWindow().setVisible(true));
    }
}
