package ui;

import model.Customer;
import service.CustomerService;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ViewCustomersPanel extends BasePanel {

    private final CustomerService customerService = new CustomerService();
    private DefaultTableModel tableModel;
    private JLabel countLabel;

    public ViewCustomersPanel(AppWindow app) {
        super(app, "View Customers", AppWindow.DASHBOARD);
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
        countLabel = new JLabel();
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        countLabel.setForeground(new Color(160, 130, 255));
        statsPanel.add(countLabel);
        topBar.add(statsPanel, BorderLayout.WEST);

        // Add Customer button (admin only)
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        btnBar.setOpaque(false);
        JButton addBtn     = makeBtn("➕  Add Customer",  new Color(100, 60, 180));
        JButton refreshBtn = makeBtn("↻  Refresh",        new Color(40, 60, 100));
        addBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        addBtn.addActionListener(e -> showAddCustomerDialog());
        refreshBtn.addActionListener(e -> loadData());
        btnBar.add(addBtn);
        btnBar.add(refreshBtn);
        topBar.add(btnBar, BorderLayout.EAST);
        wrapper.add(topBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"Customer ID", "Name", "Phone", "Latest Rental ID", "Car Rented", "Action"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 5; }
        };

        JTable table = new JTable(tableModel);
        styleTable(table);

        // Delete button column
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(table));
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setMaxWidth(120);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(22, 27, 40));
        scroll.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrapper.add(scroll, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        java.util.List<Object[]> rows = customerService.getAllCustomersWithRental();
        for (Object[] row : rows)
            tableModel.addRow(new Object[]{row[0], row[1], row[2], row[3], row[4], "🗑 Delete"});
        countLabel.setText("Total Customers: " + rows.size());
    }

    private void showAddCustomerDialog() {
        JDialog dialog = new JDialog((JFrame) app, "Add Customer", true);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(app);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(24, 30, 46));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        dialog.setContentPane(panel);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 0);

        g.gridwidth = 2; g.gridx = 0; g.gridy = 0;
        JLabel title = new JLabel("➕  Add New Customer", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(160, 120, 255));
        panel.add(title, g);

        JTextField nameField  = styledField();
        JTextField phoneField = styledField();
        addDialogRow(panel, g, 1, "Full Name:", nameField);
        addDialogRow(panel, g, 2, "Phone (10 digits):", phoneField);

        g.gridy = 3; g.gridwidth = 2; g.insets = new Insets(20, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);

        JButton saveBtn   = dialogBtn("Save",   new Color(100, 60, 180));
        JButton cancelBtn = dialogBtn("Cancel", new Color(80, 40, 40));

        saveBtn.addActionListener(e -> {
            String name  = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Both fields required.", "Error", JOptionPane.WARNING_MESSAGE); return;
            }
            if (!name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(dialog, "Name can only contain letters!", "Error", JOptionPane.WARNING_MESSAGE); return;
            }
            if (!phone.matches("[0-9]{10}")) {
                JOptionPane.showMessageDialog(dialog, "Phone must be exactly 10 digits!", "Error", JOptionPane.WARNING_MESSAGE); return;
            }
            model.Customer existing = customerService.findByPhone(phone);
            if (existing != null) {
                JOptionPane.showMessageDialog(dialog, "Phone already registered! Customer ID: " + existing.getCustomerId(), "Duplicate", JOptionPane.WARNING_MESSAGE); return;
            }
            boolean ok = customerService.registerCustomer(new model.Customer(name, phone));
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Customer added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add customer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnRow.add(saveBtn); btnRow.add(cancelBtn);
        panel.add(btnRow, g);

        dialog.setVisible(true);
    }

    private void deleteCustomer(int row) {
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(app,
            "Delete customer \"" + name + "\" (ID: " + id + ")?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = customerService.deleteCustomer(id);
        if (ok) {
            JOptionPane.showMessageDialog(app, "Customer deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(app,
                "Cannot delete — this customer has active rentals!\nReturn the car first.",
                "Cannot Delete", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    private void styleTable(JTable t) {
        t.setBackground(new Color(22, 27, 40));
        t.setForeground(Color.WHITE);
        t.setRowHeight(38);
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        t.setGridColor(new Color(40, 50, 70));
        t.setSelectionBackground(new Color(100, 60, 180));
        t.setSelectionForeground(Color.WHITE);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(18, 22, 35));
        h.setForeground(new Color(160, 130, 255));
        h.setFont(new Font("SansSerif", Font.BOLD, 14));
        h.setPreferredSize(new Dimension(0, 38));
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(SwingConstants.CENTER);
        cr.setBackground(new Color(22, 27, 40));
        cr.setForeground(Color.WHITE);
        for (int i = 0; i < 5; i++)
            t.getColumnModel().getColumn(i).setCellRenderer(cr);
    }

    // ── Delete button renderer ────────────────────────────────────────────────
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
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

    class ButtonEditor extends DefaultCellEditor {
        private JButton btn;
        private int     currentRow;

        public ButtonEditor(JTable table) {
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
                deleteCustomer(currentRow);
            });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            currentRow = r;
            btn.setText(v == null ? "" : v.toString());
            return btn;
        }
        public Object getCellEditorValue() { return btn.getText(); }
    }

    // ── Dialog helpers ────────────────────────────────────────────────────────
    private void addDialogRow(JPanel p, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = row; g.weightx = 0.4;
        g.insets = new Insets(8, 0, 8, 10);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(new Color(170, 185, 210));
        p.add(l, g);
        g.gridx = 1; g.weightx = 0.6;
        p.add(field, g);
    }
    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setBackground(new Color(18, 22, 35));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 80, 110)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return f;
    }
    private JButton dialogBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
