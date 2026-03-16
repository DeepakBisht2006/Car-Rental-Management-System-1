package ui;

import model.Car;
import service.CarService;
import javax.swing.*;
import java.awt.*;

public class AddCarPanel extends BasePanel {

    private final CarService carService = new CarService();
    private JTextField brandField, modelField, rateField;

    public AddCarPanel(AppWindow app) {
        super(app, "Add New Car", AppWindow.DASHBOARD);
        buildUI();
    }

    private void buildUI() {
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(new Color(18, 22, 35));
        contentArea.add(centre, BorderLayout.CENTER);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(24, 30, 46));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 140, 120), 2),
            BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));
        card.setPreferredSize(new Dimension(480, 380));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridwidth = 2; g.gridx = 0; g.gridy = 0;
        g.insets = new Insets(0, 0, 24, 0);
        JLabel hdr = makeLabel("➕  Add New Car", 22, true, new Color(0, 220, 180));
        hdr.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(hdr, g);

        brandField = makeField();
        modelField = makeField();
        rateField  = makeField();
        addRow(card, g, 1, "Brand:",         brandField);
        addRow(card, g, 2, "Model:",         modelField);
        addRow(card, g, 3, "Rate per Day (₹):", rateField);

        g.gridy = 4; g.gridwidth = 2; g.gridx = 0;
        g.insets = new Insets(28, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        JButton saveBtn  = makeBtn("Save Car",  new Color(0, 140, 120));
        JButton clearBtn = makeBtn("Clear",     new Color(60, 70, 100));
        saveBtn.addActionListener(e -> saveCar());
        clearBtn.addActionListener(e -> { brandField.setText(""); modelField.setText(""); rateField.setText(""); });
        btnRow.add(saveBtn); btnRow.add(clearBtn);
        card.add(btnRow, g);

        centre.add(card);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = row; g.weightx = 0.35;
        g.insets = new Insets(10, 0, 10, 12);
        JLabel l = makeLabel(label, 14, false, new Color(170, 185, 210));
        p.add(l, g);
        g.gridx = 1; g.weightx = 0.65;
        p.add(field, g);
    }

    private void saveCar() {
        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();
        String rateStr = rateField.getText().trim();
        if (brand.isEmpty() || model.isEmpty() || rateStr.isEmpty()) {
            JOptionPane.showMessageDialog(app, "All fields are required.", "Error", JOptionPane.WARNING_MESSAGE); return;
        }
        double rate;
        try { rate = Double.parseDouble(rateStr); if (rate <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(app, "Enter a valid positive rate.", "Error", JOptionPane.WARNING_MESSAGE); return;
        }
        boolean ok = carService.addCar(new Car(brand, model, rate, "YES"));
        if (ok) {
            JOptionPane.showMessageDialog(app, "Car added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            brandField.setText(""); modelField.setText(""); rateField.setText("");
        } else {
            JOptionPane.showMessageDialog(app, "Failed to add car.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
