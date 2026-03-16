package ui;

import model.Customer;
import service.CustomerService;
import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterCustomerPanel extends BasePanel {

    private final CustomerService customerService = new CustomerService();
    private JTextField nameField, phoneField;
    private JPanel     resultBox;
    private JLabel     resultLabel, resultSubLabel;

    public RegisterCustomerPanel(AppWindow app) {
        super(app, "Register Customer", AppWindow.DASHBOARD);
        buildUI();
    }

    private void buildUI() {
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(new Color(18, 22, 35));
        main.setBorder(BorderFactory.createEmptyBorder(40, 120, 40, 120));
        contentArea.add(main, BorderLayout.CENTER);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 0, 12, 0);

        // Title
        g.gridwidth = 2; g.gridx = 0; g.gridy = 0;
        JLabel hdr = makeLabel("👤  Register New Customer", 26, true, new Color(160, 120, 255));
        hdr.setHorizontalAlignment(SwingConstants.CENTER);
        main.add(hdr, g);

        // Divider
        g.gridy = 1; g.insets = new Insets(4, 0, 20, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(100, 60, 180));
        main.add(sep, g);

        // Fields
        nameField  = makeField();
        phoneField = makeField();
        g.insets = new Insets(10, 0, 10, 0);
        addRow(main, g, 2, "Full Name:", nameField);
        addRow(main, g, 3, "Phone (10 digits):", phoneField);

        // Result box
        g.gridy = 4; g.gridwidth = 2; g.insets = new Insets(28, 0, 0, 0);
        resultBox = new JPanel(new BorderLayout(0, 10));
        resultBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 100), 2),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        resultBox.setVisible(false);

        JLabel successLbl = new JLabel("", SwingConstants.CENTER);
        successLbl.setFont(new Font("SansSerif", Font.BOLD, 15));

        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 36));

        resultSubLabel = new JLabel("", SwingConstants.CENTER);
        resultSubLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));

        JPanel idBox = new JPanel(new GridLayout(3, 1, 0, 8));
        idBox.setOpaque(false);
        idBox.add(successLbl);
        idBox.add(resultLabel);
        idBox.add(resultSubLabel);
        resultBox.add(idBox, BorderLayout.CENTER);
        main.add(resultBox, g);

        // Buttons
        g.gridy = 5; g.insets = new Insets(24, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        JButton saveBtn  = makeBtn("Register",  new Color(100, 60, 180));
        JButton clearBtn = makeBtn("Clear",     new Color(60, 70, 100));
        saveBtn.addActionListener(e -> register());
        clearBtn.addActionListener(e -> {
            nameField.setText(""); phoneField.setText("");
            resultBox.setVisible(false);
        });
        btnRow.add(saveBtn); btnRow.add(clearBtn);
        main.add(btnRow, g);
    }

    private void register() {
        String name  = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        // Basic validation
        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(app, "Both fields are required.", "Error", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!name.matches("[a-zA-Z ]+")) {
            JOptionPane.showMessageDialog(app, "Name can only contain letters — no numbers or symbols!", "Invalid Name", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!phone.matches("[0-9]{10}")) {
            JOptionPane.showMessageDialog(app, "Phone must be exactly 10 digits!", "Invalid Phone", JOptionPane.WARNING_MESSAGE); return;
        }

        // Check if phone already registered (phone is unique per customer)
        Customer existingByPhone = customerService.findByPhone(phone);
        if (existingByPhone != null) {
            showAlreadyExists(existingByPhone, "This phone number is already registered!");
            return;
        }

        // Check if same name + phone combo exists
        Customer existingDuplicate = customerService.findDuplicate(name, phone);
        if (existingDuplicate != null) {
            showAlreadyExists(existingDuplicate, "This customer already exists!");
            return;
        }

        // All good — register
        boolean ok = customerService.registerCustomer(new Customer(name, phone));
        if (!ok) { JOptionPane.showMessageDialog(app, "Registration failed.", "Error", JOptionPane.ERROR_MESSAGE); return; }

        int id = getLastCustomerId();
        showSuccess(id);
    }

    /** Shows green success box with new Customer ID */
    private void showSuccess(int id) {
        resultBox.setBackground(new Color(10, 35, 20));
        resultBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 100), 2),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        JLabel successLbl = (JLabel)((JPanel)resultBox.getComponent(0)).getComponent(0);
        successLbl.setText("✅  Registration Successful!");
        successLbl.setForeground(new Color(80, 220, 120));

        resultLabel.setText("Customer ID :   " + id);
        resultLabel.setForeground(new Color(0, 255, 150));

        resultSubLabel.setText("Save this ID — you need it to rent a car!");
        resultSubLabel.setForeground(new Color(100, 170, 130));

        resultBox.setVisible(true);
        revalidate(); repaint();
    }

    /** Shows orange warning box with existing Customer ID */
    private void showAlreadyExists(Customer existing, String message) {
        resultBox.setBackground(new Color(40, 25, 5));
        resultBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 140, 0), 2),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        JLabel successLbl = (JLabel)((JPanel)resultBox.getComponent(0)).getComponent(0);
        successLbl.setText("⚠️  " + message);
        successLbl.setForeground(new Color(255, 180, 40));

        resultLabel.setText("Your existing ID :   " + existing.getCustomerId());
        resultLabel.setForeground(new Color(255, 200, 60));

        resultSubLabel.setText("Use this ID to rent a car. No need to register again!");
        resultSubLabel.setForeground(new Color(180, 140, 60));

        resultBox.setVisible(true);
        revalidate(); repaint();
    }

    private int getLastCustomerId() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT customer_id FROM customers ORDER BY customer_id DESC LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("customer_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = row; g.weightx = 0.3;
        g.insets = new Insets(10, 0, 10, 16);
        p.add(makeLabel(label, 15, false, new Color(170, 185, 210)), g);
        g.gridx = 1; g.weightx = 0.7;
        p.add(field, g);
    }
}
