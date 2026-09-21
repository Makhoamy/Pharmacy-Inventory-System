
package content;

import pharmacy.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.sql.*;

public class Billing extends JPanel {
    
    private JTextField searchField = new JTextField();
    private DefaultTableModel salesModel = new DefaultTableModel(new String[]{"Sale ID", "Date", "Cashier", "Total Amount"}, 0);
    private JTable salesTable = new JTable(salesModel);
    private JTextArea billPreview = new JTextArea();

    public Billing() {
        setLayout(new BorderLayout(8, 8));

        // Top Bar
        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(new JLabel("Search Sale ID or Cashier: "), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        JButton searchBtn = new JButton("Search Sales");
        top.add(searchBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center Split View (Sales Table on left, Bill Preview on right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(salesTable));

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        billPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        billPreview.setEditable(false);
        rightPanel.add(new JScrollPane(billPreview), BorderLayout.CENTER);

        // Buttons for Print & Save
        JPanel btnPanel = new JPanel();
        JButton printBtn = new JButton("Print Bill");
        JButton saveBtn = new JButton("Save as Text File");
        btnPanel.add(printBtn);
        btnPanel.add(saveBtn);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(550);
        add(splitPane, BorderLayout.CENTER);

        // Actions
        searchBtn.addActionListener(e -> loadSales());
        searchField.addActionListener(e -> loadSales());

        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && salesTable.getSelectedRow() >= 0) {
                int selectedRow = salesTable.getSelectedRow();
                int saleId = (int) salesModel.getValueAt(selectedRow, 0);
                generateBill(saleId);
            }
        });

        printBtn.addActionListener(e -> {
            try {
                if (billPreview.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Select a sale first.");
                    return;
                }
                billPreview.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage());
            }
        });

        saveBtn.addActionListener(e -> saveBillToFile());

        loadSales();
    }

    private void loadSales() {
        salesModel.setRowCount(0);
        String query = "SELECT s.sale_id, s.sale_date, u.full_name, s.total_amount " +
                       "FROM sales s JOIN users u ON s.user_id = u.user_id " +
                       "WHERE u.full_name LIKE ? OR CAST(s.sale_id AS CHAR) LIKE ? " +
                       "ORDER BY s.sale_id DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(query)) {
            String term = "%" + searchField.getText().trim() + "%";
            p.setString(1, term);
            p.setString(2, term);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    salesModel.addRow(new Object[]{
                        r.getInt(1), r.getTimestamp(2), r.getString(3), "R " + r.getBigDecimal(4)
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sales: " + e.getMessage());
        }
    }

    private void generateBill(int saleId) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("         HEALTHFIRST PHARMACY            \n");
        sb.append("        CUSTOMER SALES RECEIPT           \n");
        sb.append("=========================================\n");

        String saleQuery = "SELECT s.sale_id, s.sale_date, u.full_name, s.total_amount " +
                           "FROM sales s JOIN users u ON s.user_id = u.user_id WHERE s.sale_id = ?";
        String itemsQuery = "SELECT m.name, si.quantity_sold, si.price_at_sale " +
                            "FROM sale_items si JOIN medicines m ON si.medicine_id = m.medicine_id " +
                            "WHERE si.sale_id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement psSale = c.prepareStatement(saleQuery);
             PreparedStatement psItems = c.prepareStatement(itemsQuery)) {

            psSale.setInt(1, saleId);
            ResultSet rsSale = psSale.executeQuery();

            if (rsSale.next()) {
                sb.append("Receipt No : #").append(rsSale.getInt("sale_id")).append("\n");
                sb.append("Date       : ").append(rsSale.getTimestamp("sale_date")).append("\n");
                sb.append("Cashier    : ").append(rsSale.getString("full_name")).append("\n");
                sb.append("-----------------------------------------\n");
                sb.append(String.format("%-20s %-5s %-8s %-8s\n", "Item", "Qty", "Price", "Subtotal"));
                sb.append("-----------------------------------------\n");

                psItems.setInt(1, saleId);
                ResultSet rsItems = psItems.executeQuery();

                while (rsItems.next()) {
                    String name = rsItems.getString(1);
                    int qty = rsItems.getInt(2);
                    double price = rsItems.getDouble(3);
                    double subtotal = qty * price;

                    sb.append(String.format("%-20s %-5d R%-7.2f R%-7.2f\n", 
                        name.length() > 20 ? name.substring(0, 17) + "..." : name, qty, price, subtotal));
                }

                sb.append("-----------------------------------------\n");
                sb.append(String.format("TOTAL AMOUNT : R%.2f\n", rsSale.getDouble("total_amount")));
                sb.append("=========================================\n");
                sb.append("        Thank you for your visit!        \n");
                sb.append("=========================================\n");
            }

            billPreview.setText(sb.toString());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating bill: " + e.getMessage());
        }
    }

    private void saveBillToFile() {
        if (billPreview.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bill selected to save.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Bill Receipt");
        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile() + ".txt")) {
                writer.write(billPreview.getText());
                JOptionPane.showMessageDialog(this, "Bill saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save Error: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Billing Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new Billing());
            frame.setVisible(true);
        });
    }

}
