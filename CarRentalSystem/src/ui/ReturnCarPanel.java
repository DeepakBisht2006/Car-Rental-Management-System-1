package ui;

import model.Rental;
import service.CarService;
import service.RentalService;
import javax.swing.*;
import java.awt.*;

public class ReturnCarPanel extends BasePanel {

    private final RentalService rentalService = new RentalService();
    private final CarService    carService    = new CarService();
    private JTextField rentalIdField;
    private JLabel     infoLabel;

    public ReturnCarPanel(AppWindow app) {
        super(app, "Return a Car", AppWindow.DASHBOARD);
        buildUI();
    }

    private void buildUI() {
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(new Color(18, 22, 35));
        contentArea.add(centre, BorderLayout.CENTER);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(24, 30, 46));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(160, 40, 40), 2),
            BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));
        card.setPreferredSize(new Dimension(500, 360));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridwidth = 2; g.gridx = 0; g.gridy = 0;
        g.insets = new Insets(0, 0, 24, 0);

        JLabel hdr = makeLabel("↩️  Return a Car", 22, true, new Color(255, 100, 100));
        hdr.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(hdr, g);

        rentalIdField = makeField();
        g.gridwidth = 1; g.gridx = 0; g.gridy = 1; g.weightx = 0.35;
        g.insets = new Insets(10, 0, 10, 12);
        card.add(makeLabel("Rental ID:", 14, false, new Color(170, 185, 210)), g);
        g.gridx = 1; g.weightx = 0.65;
        card.add(rentalIdField, g);

        g.gridwidth = 2; g.gridx = 0; g.gridy = 2; g.insets = new Insets(4, 0, 16, 0);
        infoLabel = new JLabel("", SwingConstants.CENTER);
        infoLabel.setFont(new Font("SansSerif", Font.ITALIC, 13));
        infoLabel.setForeground(new Color(80, 200, 140));
        card.add(infoLabel, g);

        rentalIdField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { lookupRental(); }
        });

        g.gridy = 3; g.insets = new Insets(12, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);
        JButton returnBtn = makeBtn("Confirm Return", new Color(160, 40, 40));
        JButton clearBtn  = makeBtn("Clear",          new Color(60, 70, 100));
        returnBtn.addActionListener(e -> processReturn());
        clearBtn.addActionListener(e -> { rentalIdField.setText(""); infoLabel.setText(""); });
        btnRow.add(returnBtn); btnRow.add(clearBtn);
        card.add(btnRow, g);

        centre.add(card);
    }

    private void lookupRental() {
        infoLabel.setText("");
        String txt = rentalIdField.getText().trim();
        if (txt.isEmpty()) return;
        try {
            Rental r = rentalService.getRentalById(Integer.parseInt(txt));
            if (r == null) { infoLabel.setForeground(new Color(255,80,80)); infoLabel.setText("Rental not found."); }
            else if (r.getReturnDate() != null) { infoLabel.setForeground(new Color(255,180,40)); infoLabel.setText("This car has already been returned."); }
            else { infoLabel.setForeground(new Color(80,220,160)); infoLabel.setText("Car ID: "+r.getCarId()+"  |  Customer ID: "+r.getCustomerId()+"  |  Rented: "+r.getRentDate()+"  |  Days: "+r.getDays()); }
        } catch (NumberFormatException ex) { infoLabel.setForeground(new Color(255,80,80)); infoLabel.setText("Invalid Rental ID."); }
    }

    private void processReturn() {
        String txt = rentalIdField.getText().trim();
        if (txt.isEmpty()) { JOptionPane.showMessageDialog(app, "Enter a Rental ID.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        int rentalId;
        try { rentalId = Integer.parseInt(txt); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(app, "Invalid Rental ID.", "Error", JOptionPane.WARNING_MESSAGE); return; }

        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null) { JOptionPane.showMessageDialog(app, "Rental not found.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        if (rental.getReturnDate() != null) { JOptionPane.showMessageDialog(app, "This car has already been returned.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }

        int confirm = JOptionPane.showConfirmDialog(app,
            "Confirm return for Rental ID " + rentalId + "?\nCar ID: " + rental.getCarId(),
            "Confirm Return", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok1 = rentalService.returnCar(rentalId);
        boolean ok2 = carService.updateAvailability(rental.getCarId(), "YES");

        if (ok1 && ok2) {
            JOptionPane.showMessageDialog(app, "Car returned successfully!\nRental ID " + rentalId + " is now closed.", "Success", JOptionPane.INFORMATION_MESSAGE);
            rentalIdField.setText(""); infoLabel.setText("");
        } else {
            JOptionPane.showMessageDialog(app, "Return failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
