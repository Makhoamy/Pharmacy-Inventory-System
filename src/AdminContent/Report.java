package AdminContent;
import pharmacy.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Report extends JPanel {
   JComboBox<String> type = new JComboBox<>(new String[]{
        "Sales Report", "Item-Wise Report", "Low Stock Report", "Expiry Report"
    });
    DefaultTableModel m = new DefaultTableModel();
    JTable t = new JTable(m);

    public Report() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel();
        top.add(new JLabel("Report Type:"));
        top.add(type);

        JButton gen = new JButton("Generate Report");
        JButton print = new JButton("Print Report");
        top.add(gen);
        top.add(print);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(t), BorderLayout.CENTER);

        gen.addActionListener(e -> generate());
        print.addActionListener(e -> {
            try {
                t.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage());
            }
        });
        
        generate(); // Auto-load default report
    }

    void generate() {
        String x = (String) type.getSelectedItem();
        if ("Sales Report".equals(x)) sales();
        else if ("Item-Wise Report".equals(x)) items();
        else if ("Low Stock Report".equals(x)) low();
        else expiry();
    }

    void setCols(String... c) {
        m.setDataVector(new Object[0][c.length], c);
    }

    void sales() {
        setCols("Sale ID", "Date", "Cashier", "Total");
        String q = "SELECT s.sale_id, s.sale_date, u.full_name, s.total_amount " +
                   "FROM sales s JOIN users u ON s.user_id = u.user_id " +
                   "ORDER BY s.sale_date DESC";
        run(q, 4);
    }

    void items() {
        setCols("Medicine ID", "Medicine", "Quantity Sold", "Revenue");
        String q = "SELECT m.medicine_id, m.name, SUM(si.quantity_sold), SUM(si.quantity_sold * si.price_at_sale) " +
                   "FROM sale_items si JOIN medicines m ON si.medicine_id = m.medicine_id " +
                   "GROUP BY m.medicine_id, m.name ORDER BY SUM(si.quantity_sold) DESC";
        run(q, 4);
    }

    void low() {
        setCols("Medicine ID", "Medicine", "Stock", "Reorder Level");
        String q = "SELECT medicine_id, name, quantity_in_stock, reorder_level " +
                   "FROM medicines WHERE quantity_in_stock <= reorder_level " +
                   "ORDER BY quantity_in_stock";
        run(q, 4);
    }

    void expiry() {
        setCols("Medicine ID", "Medicine", "Expiry Date", "Stock");
        String q = "SELECT medicine_id, name, expiry_date, quantity_in_stock " +
                   "FROM medicines WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 MONTH) " +
                   "ORDER BY expiry_date";
        run(q, 4);
    }

    void run(String q, int n) {
        m.setRowCount(0);
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(q)) {
            while (r.next()) {
                Object[] a = new Object[n];
                for (int i = 0; i < n; i++) {
                    a[i] = r.getObject(i + 1);
                }
                m.addRow(a);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Query Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Reports Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new Report());
            frame.setVisible(true);
        });
    } 
    
}
