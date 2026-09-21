package AdminContent;

import pharmacy.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

public class Medicine extends JPanel {

    private JTextField idField = new JTextField();
    private JTextField nameField = new JTextField();
    private JTextField companyField = new JTextField();
    private JComboBox<String> typeCombo = new JComboBox<>(new String[]{
        "Tablet", "Capsule", "Syrup", "Injection", "Cream", "Ointment", "Other"
    });
    private JTextField priceField = new JTextField();
    private JTextField qtyField = new JTextField();
    private JTextField reorderField = new JTextField();
    private JTextField expiryField = new JTextField("2026-12-31");
    private JTextField supplierIdField = new JTextField();

    private DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Name", "Company", "Type", "Price (R)", "Stock", "Reorder Level", "Expiry Date", "Supplier ID"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private JTable table = new JTable(model);

    public Medicine() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridLayout(3, 6, 8, 8));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 153, 255), 1), "Medicine Details"
        ));

        idField.setEditable(false);

        formPanel.add(new JLabel("ID (Auto):")); formPanel.add(idField);
        formPanel.add(new JLabel("Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Company:")); formPanel.add(companyField);
        formPanel.add(new JLabel("Type:")); formPanel.add(typeCombo);
        formPanel.add(new JLabel("Price (R):")); formPanel.add(priceField);
        formPanel.add(new JLabel("Quantity:")); formPanel.add(qtyField);
        formPanel.add(new JLabel("Reorder Level:")); formPanel.add(reorderField);
        formPanel.add(new JLabel("Expiry (YYYY-MM-DD):")); formPanel.add(expiryField);
        formPanel.add(new JLabel("Supplier ID:")); formPanel.add(supplierIdField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton addBtn = createStyledButton("Add Medicine");
        JButton updateBtn = createStyledButton("Update");
        JButton deleteBtn = createStyledButton("Delete");
        JButton clearBtn = new JButton("Clear");
        clearBtn.setPreferredSize(new Dimension(100, 32));

        buttonPanel.add(addBtn); buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn); buttonPanel.add(clearBtn);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(Color.WHITE);
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        table.setRowHeight(24);
        table.getTableHeader().setBackground(new Color(0, 153, 255));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        addBtn.addActionListener(e -> insertMedicine());
        updateBtn.addActionListener(e -> updateMedicine());
        deleteBtn.addActionListener(e -> deleteMedicine());
        clearBtn.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                populateFormFromSelectedRow();
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

    private void loadData() {
        model.setRowCount(0);
        String sql = "SELECT medicine_id, name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id " +
                     "FROM medicines ORDER BY medicine_id";
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                model.addRow(new Object[]{
                    r.getInt("medicine_id"),
                    r.getString("name"),
                    r.getString("company"),
                    r.getString("medicine_type"),
                    r.getBigDecimal("price"),
                    r.getInt("quantity_in_stock"),
                    r.getInt("reorder_level"),
                    r.getDate("expiry_date"),
                    r.getObject("supplier_id") != null ? r.getInt("supplier_id") : "N/A"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading database records: " + e.getMessage());
        }
    }

    private void insertMedicine() {
        if (!validateInputs()) return;
        String sql = "INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            setPreparedStatementParameters(p);
            p.executeUpdate();
            JOptionPane.showMessageDialog(this, "Medicine added successfully!");
            loadData();
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding medicine: " + e.getMessage());
        }
    }

    private void updateMedicine() {
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from the table to update.");
            return;
        }
        if (!validateInputs()) return;
        String sql = "UPDATE medicines SET name=?, company=?, medicine_type=?, price=?, quantity_in_stock=?, reorder_level=?, expiry_date=?, supplier_id=? WHERE medicine_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            setPreparedStatementParameters(p);
            p.setInt(9, Integer.parseInt(idField.getText().trim()));
            p.executeUpdate();
            JOptionPane.showMessageDialog(this, "Medicine updated successfully!");
            loadData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating medicine: " + e.getMessage());
        }
    }

    private void deleteMedicine() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from the table to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete Medicine ID #" + idText + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM medicines WHERE medicine_id=?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, Integer.parseInt(idText));
                p.executeUpdate();
                JOptionPane.showMessageDialog(this, "Medicine deleted successfully!");
                loadData();
                clearForm();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting medicine: " + e.getMessage());
            }
        }
    }

    private void setPreparedStatementParameters(PreparedStatement p) throws Exception {
        p.setString(1, nameField.getText().trim());
        p.setString(2, companyField.getText().trim());
        p.setString(3, (String) typeCombo.getSelectedItem());
        p.setBigDecimal(4, new BigDecimal(priceField.getText().trim()));
        p.setInt(5, Integer.parseInt(qtyField.getText().trim()));
        p.setInt(6, Integer.parseInt(reorderField.getText().trim()));
        p.setDate(7, java.sql.Date.valueOf(expiryField.getText().trim()));
        String supplierText = supplierIdField.getText().trim();
        if (supplierText.isEmpty() || supplierText.equalsIgnoreCase("N/A")) {
            p.setNull(8, java.sql.Types.INTEGER);
        } else {
            p.setInt(8, Integer.parseInt(supplierText));
        }
    }

    private void populateFormFromSelectedRow() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        idField.setText(String.valueOf(model.getValueAt(r, 0)));
        nameField.setText(String.valueOf(model.getValueAt(r, 1)));
        companyField.setText(String.valueOf(model.getValueAt(r, 2)));
        typeCombo.setSelectedItem(String.valueOf(model.getValueAt(r, 3)));
        priceField.setText(String.valueOf(model.getValueAt(r, 4)));
        qtyField.setText(String.valueOf(model.getValueAt(r, 5)));
        reorderField.setText(String.valueOf(model.getValueAt(r, 6)));
        Object expObj = model.getValueAt(r, 7);
        expiryField.setText(expObj != null ? expObj.toString() : "2026-12-31");
        supplierIdField.setText(String.valueOf(model.getValueAt(r, 8)));
    }

    private boolean validateInputs() {
        if (nameField.getText().trim().isEmpty() || priceField.getText().trim().isEmpty() ||
            qtyField.getText().trim().isEmpty() || reorderField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
            return false;
        }
        try { 
            new BigDecimal(priceField.getText().trim());
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Price format."); return false;
        }
        try {
            Integer.parseInt(qtyField.getText().trim());
            Integer.parseInt(reorderField.getText().trim()); } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Quantity and Reorder Level must be numbers."); return false;
        }
        try { java.sql.Date.valueOf(expiryField.getText().trim()); } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Expiry date must be YYYY-MM-DD."); return false;
        }
        return true;
    }

    private void clearForm() {
        idField.setText(""); nameField.setText(""); companyField.setText("");
        typeCombo.setSelectedIndex(0); priceField.setText(""); qtyField.setText("");
        reorderField.setText(""); expiryField.setText("2026-12-31"); supplierIdField.setText("");
        table.clearSelection();
    }
}