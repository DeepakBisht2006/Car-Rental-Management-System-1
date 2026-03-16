package ui;

import model.Car;
import service.CarService;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ViewCarsPanel extends BasePanel {

    private final CarService carService = new CarService();

    public ViewCarsPanel(AppWindow app) {
        super(app, "View Cars", AppWindow.DASHBOARD);
        buildUI();
    }

    private void buildUI() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(new Color(18, 22, 35));
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        contentArea.add(wrapper, BorderLayout.CENTER);

        // Stats bar
        List<Car> cars = carService.getAllCars();
        long available = cars.stream().filter(c -> "YES".equals(c.getAvailable())).count();
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        stats.setBackground(new Color(22, 28, 42));
        stats.setBorder(BorderFactory.createMatteBorder(0,0,1,0, new Color(40,50,70)));
        stats.add(statLabel("Total Cars: " + cars.size(), new Color(180,200,240)));
        stats.add(statLabel("Available: " + available, new Color(80,220,120)));
        stats.add(statLabel("Rented: " + (cars.size() - available), new Color(255,120,100)));
        wrapper.add(stats, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Brand", "Model", "Rate/Day (₹)", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Car car : cars) {
            model.addRow(new Object[]{
                car.getCarId(), car.getBrand(), car.getModel(),
                String.format("₹ %.2f", car.getRentPerDay()),
                "YES".equals(car.getAvailable()) ? "✅  Available" : "❌  Rented"
            });
        }

        JTable table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                String status = (String) getValueAt(row, 4);
                if (status != null && status.contains("Available"))
                    c.setBackground(new Color(10, 40, 20));
                else
                    c.setBackground(new Color(40, 10, 10));
                c.setForeground(Color.WHITE);
                if (isRowSelected(row)) c.setBackground(new Color(50, 80, 130));
                return c;
            }
        };
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(22, 27, 40));
        scroll.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(scroll, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        legend.setBackground(new Color(18, 22, 35));
        legend.add(statLabel("🟢 Green = Available", new Color(80, 200, 100)));
        legend.add(statLabel("🔴 Red = Rented", new Color(220, 80, 80)));
        wrapper.add(legend, BorderLayout.SOUTH);
    }

    private JLabel statLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(color);
        return l;
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
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setCellRenderer(centre);
    }
}
