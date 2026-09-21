
package AdminContent;
import pharmacy.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Supplier extends JPanel {
    
    private JTextField idField = new JTextField();
    private JTextField nameField = new JTextField();
    private JTextField contactField = new JTextField();
    private JTextField phoneField = new JTextField();
    private JTextField emailField = new JTextField();
    private JTextField addressField = new JTextField();

    private DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Company Name", "Contact Person", "Phone", "Email", "Address"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private JTable table = new JTable(model);

    public Supplier() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // FORM PANEL
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 8, 8));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 153, 255), 1), "Supplier Details"
        ));

        idField.setEditable(false);

        formPanel.add(new JLabel("ID (Auto):"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact Person:"));
        formPanel.add(contactField);

        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(addressField);

        // BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton addBtn = createStyledButton("Add Supplier");
        JButton updateBtn = createStyledButton("Update");
        JButton deleteBtn = createStyledButton("Delete");
        JButton clearBtn = new JButton("Clear");
        clearBtn.setPreferredSize(new Dimension(100, 32));

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(clearBtn);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(btnPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // TABLE
        table.setRowHeight(24);
        table.getTableHeader().setBackground(new Color(0, 153, 255));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // LISTENERS
        loadSuppliers();

        addBtn.addActionListener(e -> insertSupplier());
        updateBtn.addActionListener(e -> updateSupplier());
        deleteBtn.addActionListener(e -> deleteSupplier());
        clearBtn.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                populateForm();
            }
        });
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0, 153, 255));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 32));
        return btn;
    }

    private void loadSuppliers() {
        model.setRowCount(0);
        String sql = "SELECT supplier_id, name, contact_person, phone, email, address FROM suppliers ORDER BY supplier_id";
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                model.addRow(new Object[]{
                    r.getInt("supplier_id"),
                    r.getString("name"),
                    r.getString("contact_person"),
                    r.getString("phone"),
                    r.getString("email"),
                    r.getString("address")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading suppliers: " + e.getMessage());
        }
    }

    private void insertSupplier() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Supplier Name is required.");
            return;
        }

        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, nameField.getText().trim());
            p.setString(2, contactField.getText().trim());
            p.setString(3, phoneField.getText().trim());
            p.setString(4, emailField.getText().trim());
            p.setString(5, addressField.getText().trim());

            p.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier added successfully!");
            loadSuppliers();
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inserting supplier: " + e.getMessage());
        }
    }

    private void updateSupplier() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a supplier to update.");
            return;
        }

        String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE supplier_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, nameField.getText().trim());
            p.setString(2, contactField.getText().trim());
            p.setString(3, phoneField.getText().trim());
            p.setString(4, emailField.getText().trim());
            p.setString(5, addressField.getText().trim());
            p.setInt(6, Integer.parseInt(idText));

            p.executeUpdate();
            JOptionPane.showMessageDialog(this, "Supplier updated successfully!");
            loadSuppliers();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating supplier: " + e.getMessage());
        }
    }

    private void deleteSupplier() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a supplier to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Supplier #" + idText + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM suppliers WHERE supplier_id=?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement p = c.prepareStatement(sql)) {

                p.setInt(1, Integer.parseInt(idText));
                p.executeUpdate();
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully!");
                loadSuppliers();
                clearForm();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting supplier: " + e.getMessage());
            }
        }
    }

    private void populateForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        idField.setText(String.valueOf(model.getValueAt(r, 0)));
        nameField.setText(String.valueOf(model.getValueAt(r, 1)));
        contactField.setText(String.valueOf(model.getValueAt(r, 2)));
        phoneField.setText(String.valueOf(model.getValueAt(r, 3)));
        emailField.setText(String.valueOf(model.getValueAt(r, 4)));
        addressField.setText(String.valueOf(model.getValueAt(r, 5)));
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        contactField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        table.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Supplier Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new Supplier());
            frame.setVisible(true);
        });
    }
}

