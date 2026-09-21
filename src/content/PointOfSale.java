
package content;
import pharmacy.DatabaseConnection;
import pharmacy.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;

public class PointOfSale extends JPanel {
  private JTextField searchField = new JTextField(20);
    private JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

    private DefaultTableModel resultsModel = new DefaultTableModel(
        new String[]{"ID", "Medicine Name", "Type", "Price (R)", "Available Stock"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private JTable resultsTable = new JTable(resultsModel);

    private DefaultTableModel cartModel = new DefaultTableModel(
        new String[]{"ID", "Medicine Name", "Price (R)", "Qty", "Subtotal (R)"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private JTable cartTable = new JTable(cartModel);

    private JLabel totalLabel = new JLabel("Total: R0.00");

    public PointOfSale() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);

        // TOP SEARCH BAR
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Search Medicine:"));
        topPanel.add(searchField);
        JButton searchBtn = createStyledButton("Search");
        topPanel.add(searchBtn);

        add(topPanel, BorderLayout.NORTH);

        // CENTER SPLIT (Results Table + Cart Table)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setBackground(Color.WHITE);

        resultsTable.setRowHeight(22);
        resultsTable.getTableHeader().setBackground(new Color(0, 153, 255));
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        centerPanel.add(new JScrollPane(resultsTable));

        JPanel lowerPanel = new JPanel(new BorderLayout(5, 5));
        lowerPanel.setBackground(Color.WHITE);

        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolsPanel.setBackground(Color.WHITE);
        toolsPanel.add(new JLabel("Quantity:"));
        toolsPanel.add(qtySpinner);

        JButton addBtn = createStyledButton("Add to Cart");
        JButton removeBtn = createStyledButton("Remove");
        JButton clearBtn = new JButton("Clear Cart");
        JButton checkoutBtn = createStyledButton("Checkout");

        toolsPanel.add(addBtn);
        toolsPanel.add(removeBtn);
        toolsPanel.add(clearBtn);
        toolsPanel.add(checkoutBtn);

        lowerPanel.add(toolsPanel, BorderLayout.NORTH);

        cartTable.setRowHeight(22);
        cartTable.getTableHeader().setBackground(new Color(0, 153, 255));
        cartTable.getTableHeader().setForeground(Color.WHITE);
        lowerPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        centerPanel.add(lowerPanel);
        add(centerPanel, BorderLayout.CENTER);

        // BOTTOM SUMMARY BAR
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(0, 153, 255));
        southPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(Color.WHITE);
        southPanel.add(totalLabel, BorderLayout.WEST);

        add(southPanel, BorderLayout.SOUTH);

        // LISTENERS
        searchBtn.addActionListener(e -> searchMedicines());
        searchField.addActionListener(e -> searchMedicines());
        addBtn.addActionListener(e -> addToCart());
        removeBtn.addActionListener(e -> removeFromCart());
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkout());

        searchMedicines();
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0, 153, 255));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        return btn;
    }

    private void searchMedicines() {
        resultsModel.setRowCount(0);
        String sql = "SELECT medicine_id, name, medicine_type, price, quantity_in_stock FROM medicines WHERE name LIKE ? ORDER BY name";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, "%" + searchField.getText().trim() + "%");
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    resultsModel.addRow(new Object[]{
                        r.getInt("medicine_id"),
                        r.getString("name"),
                        r.getString("medicine_type"),
                        r.getBigDecimal("price"),
                        r.getInt("quantity_in_stock")
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching stock: " + e.getMessage());
        }
    }

    private void addToCart() {
        int r = resultsTable.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine from the top table.");
            return;
        }

        int medId = (int) resultsModel.getValueAt(r, 0);
        String name = (String) resultsModel.getValueAt(r, 1);
        BigDecimal price = (BigDecimal) resultsModel.getValueAt(r, 3);
        int stock = (int) resultsModel.getValueAt(r, 4);
        int qty = (int) qtySpinner.getValue();

        // Check stock availability
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            if ((int) cartModel.getValueAt(i, 0) == medId) {
                int existingQty = (int) cartModel.getValueAt(i, 3);
                int newQty = existingQty + qty;
                if (newQty > stock) {
                    JOptionPane.showMessageDialog(this, "Quantity exceeds available stock (" + stock + ").");
                    return;
                }
                cartModel.setValueAt(newQty, i, 3);
                cartModel.setValueAt(price.multiply(BigDecimal.valueOf(newQty)), i, 4);
                calculateTotal();
                return;
            }
        }

        if (qty > stock) {
            JOptionPane.showMessageDialog(this, "Not enough stock available (" + stock + ").");
            return;
        }

        cartModel.addRow(new Object[]{medId, name, price, qty, price.multiply(BigDecimal.valueOf(qty))});
        calculateTotal();
    }

    private void removeFromCart() {
        int r = cartTable.getSelectedRow();
        if (r >= 0) {
            cartModel.removeRow(r);
            calculateTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Select an item in the cart to remove.");
        }
    }

    private void clearCart() {
        cartModel.setRowCount(0);
        calculateTotal();
    }

    private void calculateTotal() {
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (int i = 0; i < cartModel.getRowCount(); i++) {
            grandTotal = grandTotal.add((BigDecimal) cartModel.getValueAt(i, 4));
        }
        totalLabel.setText("Total: R" + grandTotal.setScale(2, RoundingMode.HALF_UP));
    }

    private void checkout() {
        if (cartModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty.");
            return;
        }

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            BigDecimal grandTotal = BigDecimal.ZERO;
            for (int i = 0; i < cartModel.getRowCount(); i++) {
                grandTotal = grandTotal.add((BigDecimal) cartModel.getValueAt(i, 4));
            }

            // 1. Insert into Table 4: sales
            String insertSale = "INSERT INTO sales (total_amount, user_id) VALUES (?, ?)";
            PreparedStatement psSale = c.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS);
            psSale.setBigDecimal(1, grandTotal);
            psSale.setInt(2, UserSession.userId > 0 ? UserSession.userId : 1);
            psSale.executeUpdate();

            ResultSet keys = psSale.getGeneratedKeys();
            keys.next();
            int saleId = keys.getInt(1);

            // 2. Insert into Table 5: sale_items and update Table 3: medicines
            String insertItem = "INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES (?, ?, ?, ?)";
            String updateStock = "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id = ? AND quantity_in_stock >= ?";

            PreparedStatement psItem = c.prepareStatement(insertItem);
            PreparedStatement psStock = c.prepareStatement(updateStock);

            for (int i = 0; i < cartModel.getRowCount(); i++) {
                int medId = (int) cartModel.getValueAt(i, 0);
                BigDecimal price = (BigDecimal) cartModel.getValueAt(i, 2);
                int qty = (int) cartModel.getValueAt(i, 3);

                psItem.setInt(1, saleId);
                psItem.setInt(2, medId);
                psItem.setInt(3, qty);
                psItem.setBigDecimal(4, price);
                psItem.executeUpdate();

                psStock.setInt(1, qty);
                psStock.setInt(2, medId);
                psStock.setInt(3, qty);

                if (psStock.executeUpdate() != 1) {
                    throw new SQLException("Stock changed during transaction for item ID: " + medId);
                }
            }

            c.commit();
            JOptionPane.showMessageDialog(this, "Sale #" + saleId + " completed successfully!");
            clearCart();
            searchMedicines();

        } catch (Exception ex) {
            try { if (c != null) c.rollback(); } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage());
        } finally {
            try { if (c != null) c.close(); } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("POS Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new PointOfSale());
            frame.setVisible(true);
        });
    }
  
    
}
