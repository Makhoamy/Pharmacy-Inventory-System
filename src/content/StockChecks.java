
package content;

import pharmacy.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StockChecks extends JPanel {
 private JTextField searchField = new JTextField(20);
    private DefaultTableModel model = new DefaultTableModel(
        new String[]{"ID", "Medicine Name", "Company", "Type", "Price (R)", "Stock Available", "Reorder Alert", "Expiry Date"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private JTable table = new JTable(model);

    public StockChecks() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);

        // TOP BAR
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Search Medicine:"));
        topPanel.add(searchField);

        JButton checkBtn = new JButton("Check Stock");
        checkBtn.setBackground(new Color(0, 153, 255));
        checkBtn.setForeground(Color.WHITE);
        checkBtn.setFont(new Font("Arial", Font.BOLD, 12));
        topPanel.add(checkBtn);

        add(topPanel, BorderLayout.NORTH);

        // TABLE
        table.setRowHeight(24);
        table.getTableHeader().setBackground(new Color(0, 153, 255));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // LISTENERS
        checkBtn.addActionListener(e -> loadStock());
        searchField.addActionListener(e -> loadStock());

        loadStock();
    }

    private void loadStock() {
        model.setRowCount(0);
        String sql = "SELECT medicine_id, name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date " +
                     "FROM medicines WHERE name LIKE ? ORDER BY name";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, "%" + searchField.getText().trim() + "%");
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    int stock = r.getInt("quantity_in_stock");
                    int reorder = r.getInt("reorder_level");
                    String alert = (stock <= reorder) ? "LOW STOCK" : "OK";

                    model.addRow(new Object[]{
                        r.getInt("medicine_id"),
                        r.getString("name"),
                        r.getString("company"),
                        r.getString("medicine_type"),
                        r.getBigDecimal("price"),
                        stock,
                        alert,
                        r.getDate("expiry_date")
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error checking stock: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Stock Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new StockChecks());
            frame.setVisible(true);
        });
    }
}   
    

