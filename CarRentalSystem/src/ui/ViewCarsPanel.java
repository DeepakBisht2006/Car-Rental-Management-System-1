package ui;

import model.Car;
import service.CarService;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ViewCarsPanel extends BasePanel {

    private final CarService carService = new CarService();
    private DefaultTableModel tableModel;
    private JLabel totalLbl, availLbl, rentedLbl;

    public ViewCarsPanel(AppWindow app) {
        super(app, "View Cars", AppWindow.DASHBOARD);
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(new Color(18, 22, 35));
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        contentArea.add(wrapper, BorderLayout.CENTER);

        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(22, 28, 42));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 50, 70)));

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statsPanel.setOpaque(false);
        totalLbl  = statLabel("", new Color(180, 200, 240));
        availLbl  = statLabel("", new Color(80, 220, 120));
        rentedLbl = statLabel("", new Color(255, 120, 100));
        statsPanel.add(totalLbl);
        statsPanel.add(availLbl);
        statsPanel.add(rentedLbl);
        topBar.add(statsPanel, BorderLayout.WEST);

        // Refresh button (only show delete col for admin)
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        btnBar.setOpaque(false);
        JButton refreshBtn = makeBtn("↻  Refresh", new Color(40, 60, 100));
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.addActionListener(e -> loadData());
        btnBar.add(refreshBtn);
        topBar.add(btnBar, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        boolean isAdmin = app.isAdmin();
        String[] cols = isAdmin
            ? new String[]{"ID", "Brand", "Model", "Rate/Day (₹)", "Status", "Action"}
            : new String[]{"ID", "Brand", "Model", "Rate/Day (₹)", "Status"};

        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return isAdmin && c == 5;
            }
        };

        JTable table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                String status = (String) getValueAt(row, 4);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(50, 80, 130));
                } else if (status != null && status.contains("Available")) {
                    c.setBackground(new Color(10, 40, 20));
                } else {
                    c.setBackground(new Color(40, 10, 10));
                }
                c.setForeground(Color.WHITE);
                return c;
            }
        };

        styleTable(table);

        if (isAdmin) {
            table.getColumnModel().getColumn(5).setCellRenderer(new BtnRenderer());
            table.getColumnModel().getColumn(5).setCellEditor(new BtnEditor(table));
            table.getColumnModel().getColumn(5).setPreferredWidth(100);
            table.getColumnModel().getColumn(5).setMaxWidth(120);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(22, 27, 40));
        scroll.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(scroll, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        legend.setBackground(new Color(18, 22, 35));
        legend.add(statLabel("🟢 Green = Available", new Color(80, 200, 100)));
        legend.add(statLabel("🔴 Red = Rented",      new Color(220, 80, 80)));
        wrapper.add(legend, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Car> cars = carService.getAllCars();
        long available = cars.stream().filter(c -> "YES".equals(c.getAvailable())).count();
        totalLbl.setText("Total Cars: " + cars.size());
        availLbl.setText("Available: " + available);
        rentedLbl.setText("Rented: " + (cars.size() - available));

        for (Car car : cars) {
            String status = "YES".equals(car.getAvailable()) ? "✅  Available" : "❌  Rented";
            if (app.isAdmin()) {
                tableModel.addRow(new Object[]{
                    car.getCarId(), car.getBrand(), car.getModel(),
                    String.format("₹ %.2f", car.getRentPerDay()),
                    status, "🗑 Delete"
                });
            } else {
                tableModel.addRow(new Object[]{
                    car.getCarId(), car.getBrand(), car.getModel(),
                    String.format("₹ %.2f", car.getRentPerDay()),
                    status
                });
            }
        }
    }

    private void deleteCar(int row) {
        int id       = (int)    tableModel.getValueAt(row, 0);
        String brand = (String) tableModel.getValueAt(row, 1);
        String model = (String) tableModel.getValueAt(row, 2);
        String status = (String) tableModel.getValueAt(row, 4);

        if (status.contains("Rented")) {
            JOptionPane.showMessageDialog(app,
                "Cannot delete — " + brand + " " + model + " is currently rented!\nReturn the car first.",
                "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(app,
            "Delete " + brand + " " + model + " (ID: " + id + ")?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = carService.deleteCar(id);
        if (ok) {
            JOptionPane.showMessageDialog(app, brand + " " + model + " deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(app, "Failed to delete car.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable t) {
        t.setRowHeight(36);
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        t.setGridColor(new Color(40, 50, 70));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(18, 22, 35));
        h.setForeground(new Color(0, 200, 180));
        h.setFont(new Font("SansSerif", Font.BOLD, 14));
        h.setPreferredSize(new Dimension(0, 38));
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        int colCount = app.isAdmin() ? 5 : 5;
        for (int i = 0; i < colCount; i++)
            t.getColumnModel().getColumn(i).setCellRenderer(cr);
    }

    private JLabel statLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(color);
        return l;
    }

    // ── Delete button renderer ────────────────────────────────────────────────
    class BtnRenderer extends JButton implements TableCellRenderer {
        public BtnRenderer() {
            setOpaque(true);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setBackground(new Color(140, 30, 30));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            setText(v == null ? "" : v.toString());
            return this;
        }
    }

    class BtnEditor extends DefaultCellEditor {
        private JButton btn;
        private int     currentRow;

        public BtnEditor(JTable table) {
            super(new JCheckBox());
            btn = new JButton();
            btn.setOpaque(true);
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setBackground(new Color(140, 30, 30));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            btn.addActionListener(e -> {
                fireEditingStopped();
                deleteCar(currentRow);
            });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            currentRow = r;
            btn.setText(v == null ? "" : v.toString());
            return btn;
        }
        public Object getCellEditorValue() { return btn.getText(); }
    }
}
