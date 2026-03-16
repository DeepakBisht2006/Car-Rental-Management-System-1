package service;

import database.DBConnection;
import model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    public boolean registerCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerService.registerCustomer] Error: " + e.getMessage());
            return false;
        }
    }

    /** Returns existing customer if name AND phone both match, else null */
    public Customer findDuplicate(String name, String phone) {
        String sql = "SELECT * FROM customers WHERE LOWER(name) = LOWER(?) AND phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, phone.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[CustomerService.findDuplicate] Error: " + e.getMessage());
        }
        return null;
    }

    /** Returns existing customer if phone matches (phone should be unique) */
    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[CustomerService.findByPhone] Error: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM customers");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) customers.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[CustomerService.getAllCustomers] Error: " + e.getMessage());
        }
        return customers;
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[CustomerService.getCustomerById] Error: " + e.getMessage());
        }
        return null;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(rs.getInt("customer_id"), rs.getString("name"), rs.getString("phone"));
    }

    /** Deletes a customer by ID. Returns false if customer has active rentals. */
    public boolean deleteCustomer(int customerId) {
        // Check for active rentals first
        String checkSql = "SELECT COUNT(*) FROM rentals WHERE customer_id = ? AND return_date IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false; // has active rentals
        } catch (SQLException e) {
            System.err.println("[CustomerService.deleteCustomer] Check error: " + e.getMessage());
            return false;
        }
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerService.deleteCustomer] Error: " + e.getMessage());
            return false;
        }
    }


    /** Gets all customers with their latest rental ID and car name */
    public List<Object[]> getAllCustomersWithRental() {
        List<Object[]> rows = new ArrayList<>();
        String sql =
            "SELECT c.customer_id, c.name, c.phone, " +
            "       r.rental_id, " +
            "       CONCAT(ca.brand, ' ', ca.model) AS car_name " +
            "FROM customers c " +
            "LEFT JOIN rentals r ON r.rental_id = (" +
            "    SELECT MAX(r2.rental_id) FROM rentals r2 WHERE r2.customer_id = c.customer_id" +
            ") " +
            "LEFT JOIN cars ca ON ca.car_id = r.car_id " +
            "ORDER BY c.customer_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int rentalId = rs.getInt("rental_id");
                String carName = rs.getString("car_name");
                rows.add(new Object[]{
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rentalId == 0 ? "None" : String.valueOf(rentalId),
                    carName != null ? carName : "None"
                });
            }
        } catch (SQLException e) {
            System.err.println("[CustomerService.getAllCustomersWithRental] Error: " + e.getMessage());
        }
        return rows;
    }

}