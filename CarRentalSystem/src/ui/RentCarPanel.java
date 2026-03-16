package ui;

import model.Car;
import model.Customer;
import model.Rental;
import service.CarService;
import service.CustomerService;
import service.RentalService;
import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class RentCarPanel extends BasePanel {

    private final CarService      carService      = new CarService();
    private final CustomerService customerService = new CustomerService();
    private final RentalService   rentalService   = new RentalService();

    private JTextField carIdField, customerIdField, daysField;
    private JLabel     carInfoLabel, customerInfoLabel, totalLabel;
    private Car        currentCar;

    // Payment sub-panel references
    private JPanel mainForm, paymentPanel;
    private JLabel qrLabel, payAmountLabel;
    private double  pendingTotal;
    private Rental  pendingRental;

    public RentCarPanel(AppWindow app) {
        super(app, "Rent a Car", AppWindow.DASHBOARD);
        buildUI();
    }

    private void buildUI() {
        JPanel outer = new JPanel(new CardLayout());
        outer.setBackground(new Color(18, 22, 35));
        contentArea.add(outer, BorderLayout.CENTER);

        // ── MAIN FORM ──────────────────────────────────────────────────────────
        mainForm = new JPanel(new GridBagLayout());
        mainForm.setBackground(new Color(18, 22, 35));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(24, 30, 46));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 110, 0), 2),
            BorderFactory.createEmptyBorder(36, 60, 36, 60)
        ));
        card.setPreferredSize(new Dimension(560, 480));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 0);

        g.gridwidth = 2; g.gridx = 0; g.gridy = 0;
        JLabel hdr = makeLabel("🚘  Rent a Car", 22, true, new Color(255, 180, 40));
        hdr.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(hdr, g);
        g.insets = new Insets(12, 0, 4, 0);

        carIdField       = makeField();
        customerIdField  = makeField();
        daysField        = makeField();
        carInfoLabel      = infoLabel();
        customerInfoLabel = infoLabel();
        totalLabel        = makeLabel("", 14, true, new Color(255, 200, 60));

        addFormRow(card, g, 1, "Car ID:", carIdField, carInfoLabel);
        addFormRow(card, g, 3, "Customer ID:", customerIdField, customerInfoLabel);
        addFormRow(card, g, 5, "Number of Days:", daysField, null);

        g.gridy = 7; g.gridwidth = 2; g.insets = new Insets(6, 0, 16, 0);
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(totalLabel, g);

        // Auto-lookup on focus loss
        carIdField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { lookupCar(); calcTotal(); }
        });
        customerIdField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { lookupCustomer(); }
        });
        daysField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { calcTotal(); }
        });

        g.gridy = 8; g.insets = new Insets(0, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);
        JButton confirmBtn = makeBtn("Confirm Rental →", new Color(180, 110, 0));
        JButton clearBtn   = makeBtn("Clear", new Color(60, 70, 100));
        confirmBtn.addActionListener(e -> processRental());
        clearBtn.addActionListener(e -> clearForm());
        btnRow.add(confirmBtn); btnRow.add(clearBtn);
        card.add(btnRow, g);

        mainForm.add(card);
        outer.add(mainForm, "form");

        // ── PAYMENT PANEL ──────────────────────────────────────────────────────
        paymentPanel = buildPaymentPanel(outer);
        outer.add(paymentPanel, "payment");
    }

    private JPanel buildPaymentPanel(JPanel outer) {
        JPanel pp = new JPanel(new GridBagLayout());
        pp.setBackground(new Color(18, 22, 35));

        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(new Color(24, 30, 46));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 120), 2),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(440, 560));

        // Header
        JLabel hdr = makeLabel("💳  Scan & Pay", 22, true, new Color(0, 230, 140));
        hdr.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(hdr, BorderLayout.NORTH);

        // QR area
        JPanel qrArea = new JPanel(new BorderLayout(0, 10));
        qrArea.setOpaque(false);

        qrLabel = new JLabel("Loading QR...", SwingConstants.CENTER);
        qrLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        qrLabel.setForeground(new Color(100, 130, 160));
        qrLabel.setPreferredSize(new Dimension(220, 220));
        qrLabel.setOpaque(true);
        qrLabel.setBackground(new Color(24, 30, 46));

        payAmountLabel = makeLabel("", 26, true, new Color(0, 230, 140));
        payAmountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel upiHint = new JLabel("Scan with Google Pay  •  PhonePe  •  Paytm", SwingConstants.CENTER);
        upiHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        upiHint.setForeground(new Color(80, 100, 130));

        JPanel qrWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        qrWrapper.setOpaque(false);
        qrWrapper.add(qrLabel);

        qrArea.add(qrWrapper,     BorderLayout.CENTER);
        qrArea.add(payAmountLabel, BorderLayout.NORTH);
        qrArea.add(upiHint,       BorderLayout.SOUTH);
        card.add(qrArea, BorderLayout.CENTER);

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 14, 0));
        btnRow.setOpaque(false);
        JButton paidBtn   = makeBtn("✅  I Have Paid",  new Color(0, 140, 80));
        JButton cancelBtn = makeBtn("✖  Cancel",       new Color(140, 40, 40));
        paidBtn.addActionListener(e -> confirmPayment(outer));
        cancelBtn.addActionListener(e -> {
            ((CardLayout)outer.getLayout()).show(outer, "form");
            JOptionPane.showMessageDialog(app, "Payment cancelled.", "Cancelled", JOptionPane.WARNING_MESSAGE);
        });
        btnRow.add(paidBtn); btnRow.add(cancelBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        pp.add(card);
        return pp;
    }

    private void processRental() {
        lookupCar(); lookupCustomer();
        if (currentCar == null) {
            JOptionPane.showMessageDialog(app, "Please enter a valid available Car ID.", "Error", JOptionPane.WARNING_MESSAGE); return;
        }
        int customerId, days;
        try {
            customerId = Integer.parseInt(customerIdField.getText().trim());
            days       = Integer.parseInt(daysField.getText().trim());
            if (days <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(app, "Customer ID and Days must be valid positive numbers.", "Error", JOptionPane.WARNING_MESSAGE); return;
        }
        if (customerService.getCustomerById(customerId) == null) {
            JOptionPane.showMessageDialog(app, "Customer ID not found.", "Error", JOptionPane.WARNING_MESSAGE); return;
        }
        pendingTotal  = days * currentCar.getRentPerDay();
        pendingRental = new Rental(currentCar.getCarId(), customerId, LocalDate.now(), days, pendingTotal);

        // Show payment panel
        payAmountLabel.setText(String.format("Total: ₹ %.2f", pendingTotal));
        loadQR(pendingTotal);
        JPanel outer = (JPanel) mainForm.getParent();
        ((CardLayout) outer.getLayout()).show(outer, "payment");
    }

    private void confirmPayment(JPanel outer) {
        int confirm = JOptionPane.showConfirmDialog(app,
            String.format("Confirm payment of ₹ %.2f?", pendingTotal),
            "Confirm Payment", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean rentalOk = rentalService.createRental(pendingRental);
        boolean carOk    = carService.updateAvailability(currentCar.getCarId(), "NO");

        if (rentalOk && carOk) {
            int rentalId = getLastRentalId();
            ((CardLayout) outer.getLayout()).show(outer, "form");
            JOptionPane.showMessageDialog(app,
                String.format("Rental Confirmed!\n\nYour Rental ID:  %d\n\n%s %s rented for %d days.\nTotal: ₹ %.2f\n\nSave your Rental ID to return the car!",
                    rentalId, currentCar.getBrand(), currentCar.getModel(), pendingRental.getDays(), pendingTotal),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } else {
            JOptionPane.showMessageDialog(app, "Rental failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadQR(double amount) {
        qrLabel.setIcon(null); qrLabel.setText("Loading QR...");
        SwingWorker<ImageIcon, Void> w = new SwingWorker<>() {
            protected ImageIcon doInBackground() {
                try {
                    String upi = String.format("upi://pay?pa=carrental@upi&pn=CarRental&am=%.2f&cu=INR", amount);
                    String url = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&margin=10&data="
                        + java.net.URLEncoder.encode(upi, "UTF-8");
                    BufferedImage img = ImageIO.read(new URL(url));
                    if (img != null) return new ImageIcon(img);
                } catch (IOException ignored) {}
                return null;
            }
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) { qrLabel.setIcon(icon); qrLabel.setText(""); }
                    else qrLabel.setText("<html><center>No internet.<br>Pay manually to<br><b>carrental@upi</b></center></html>");
                } catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void lookupCar() {
        carInfoLabel.setText("");
        String txt = carIdField.getText().trim();
        if (txt.isEmpty()) return;
        try {
            Car car = carService.getCarById(Integer.parseInt(txt));
            if (car == null) { carInfoLabel.setForeground(new Color(255,80,80)); carInfoLabel.setText("Car not found."); }
            else if (!"YES".equals(car.getAvailable())) { carInfoLabel.setForeground(new Color(255,180,40)); carInfoLabel.setText(car.getBrand()+" "+car.getModel()+" — NOT available."); }
            else { currentCar = car; carInfoLabel.setForeground(new Color(80,220,160)); carInfoLabel.setText(car.getBrand()+" "+car.getModel()+"  |  ₹"+String.format("%.2f",car.getRentPerDay())+"/day"); }
        } catch (NumberFormatException ex) { carInfoLabel.setForeground(new Color(255,80,80)); carInfoLabel.setText("Invalid ID."); }
    }

    private void lookupCustomer() {
        customerInfoLabel.setText("");
        String txt = customerIdField.getText().trim();
        if (txt.isEmpty()) return;
        try {
            Customer c = customerService.getCustomerById(Integer.parseInt(txt));
            if (c == null) { customerInfoLabel.setForeground(new Color(255,80,80)); customerInfoLabel.setText("Customer not found."); }
            else { customerInfoLabel.setForeground(new Color(80,220,160)); customerInfoLabel.setText(c.getName()+"  |  "+c.getPhone()); }
        } catch (NumberFormatException ex) { customerInfoLabel.setForeground(new Color(255,80,80)); customerInfoLabel.setText("Invalid ID."); }
    }

    private void calcTotal() {
        if (currentCar == null) return;
        try {
            int days = Integer.parseInt(daysField.getText().trim());
            double total = days * currentCar.getRentPerDay();
            totalLabel.setText(String.format("Total Cost: ₹ %.2f   (%d days × ₹ %.2f/day)", total, days, currentCar.getRentPerDay()));
        } catch (NumberFormatException ignored) { totalLabel.setText(""); }
    }

    private void clearForm() {
        carIdField.setText(""); customerIdField.setText(""); daysField.setText("");
        carInfoLabel.setText(""); customerInfoLabel.setText(""); totalLabel.setText("");
        currentCar = null; pendingRental = null;
    }

    private int getLastRentalId() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT rental_id FROM rentals ORDER BY rental_id DESC LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("rental_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    private JLabel infoLabel() {
        JLabel l = new JLabel("");
        l.setFont(new Font("SansSerif", Font.ITALIC, 12));
        l.setForeground(new Color(80, 200, 140));
        return l;
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int row, String label, JTextField field, JLabel info) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = row; g.weightx = 0.3;
        g.insets = new Insets(8, 0, 2, 12);
        p.add(makeLabel(label, 14, false, new Color(170, 185, 210)), g);
        g.gridx = 1; g.weightx = 0.7;
        p.add(field, g);
        if (info != null) {
            g.gridy = row + 1; g.gridx = 1; g.insets = new Insets(0, 0, 6, 0);
            p.add(info, g);
        }
    }
}
