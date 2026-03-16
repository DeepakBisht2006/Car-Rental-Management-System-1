package ui;

import javax.swing.*;
import java.awt.*;

/**
 * Single application window using CardLayout.
 * Every panel is rebuilt fresh on each navigation to avoid stale state.
 */
public class AppWindow extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);
    private boolean isAdmin = false;

    public static final String LOGIN             = "login";
    public static final String DASHBOARD         = "dashboard";
    public static final String ADD_CAR           = "addCar";
    public static final String VIEW_CARS         = "viewCars";
    public static final String VIEW_CUSTOMERS    = "viewCustomers";
    public static final String REGISTER_CUSTOMER = "registerCustomer";
    public static final String RENT_CAR          = "rentCar";
    public static final String RETURN_CAR        = "returnCar";

    public AppWindow() {
        setTitle("Car Rental Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(900, 600));

        cardPanel.setBackground(new Color(18, 22, 35));
        setContentPane(cardPanel);

        // Start with login screen
        forceShowLogin();
    }

    /**
     * Navigate to any panel — always rebuilds fresh so data is current
     * and buttons always work.
     */
    public void showPanel(String name) {
        // Remove existing panel with this name if present
        removePanel(name);

        // Build a fresh panel
        JPanel panel = buildPanel(name);
        if (panel == null) return;

        cardPanel.add(panel, name);
        cardLayout.show(cardPanel, name);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    /** Removes any existing panel registered under the given name */
    private void removePanel(String name) {
        // We tag each panel with its name so we can find it
        for (Component c : cardPanel.getComponents()) {
            if (name.equals(c.getName())) {
                cardPanel.remove(c);
                break;
            }
        }
    }

    /** Factory: create the right panel for a given name */
    private JPanel buildPanel(String name) {
        switch (name) {
            case LOGIN:             return tag(new LoginPanel(this),             LOGIN);
            case DASHBOARD:        return tag(new DashboardPanel(this),         DASHBOARD);
            case ADD_CAR:          return tag(new AddCarPanel(this),            ADD_CAR);
            case VIEW_CARS:        return tag(new ViewCarsPanel(this),          VIEW_CARS);
            case VIEW_CUSTOMERS:   return tag(new ViewCustomersPanel(this),     VIEW_CUSTOMERS);
            case REGISTER_CUSTOMER:return tag(new RegisterCustomerPanel(this),  REGISTER_CUSTOMER);
            case RENT_CAR:         return tag(new RentCarPanel(this),           RENT_CAR);
            case RETURN_CAR:       return tag(new ReturnCarPanel(this),         RETURN_CAR);
            default: return null;
        }
    }

    /** Tags a panel with its name so we can find and remove it later */
    private JPanel tag(JPanel panel, String name) {
        panel.setName(name);
        return panel;
    }

    /** Rebuilds and shows a completely fresh login panel */
    public void forceShowLogin() {
        setAdmin(false);
        showPanel(LOGIN);
    }

    public void setAdmin(boolean admin) { this.isAdmin = admin; }
    public boolean isAdmin()            { return isAdmin; }
}
