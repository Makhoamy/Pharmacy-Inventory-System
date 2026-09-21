package pharmacy;

import content.*;
import javax.swing.*;
import java.awt.*;

public class Cashier extends JFrame {

    public Cashier() {
        setTitle("HealthFirst Pharmacy - Cashier Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);

        // Header Panel with Cyan Blue theme
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 153, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel header = new JLabel("Welcome, " + (UserSession.fullName != null ? UserSession.fullName : "Cashier") + " | Cashier Module", SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setForeground(Color.WHITE);
        headerPanel.add(header, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Cashier Tabbed Navigation
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));

        tabs.addTab("Point of Sale (POS)", new PointOfSale());
        tabs.addTab("Billing & Receipts", new Billing());
        tabs.addTab("Stock Check", new StockChecks());

        add(tabs, BorderLayout.CENTER);

        // Top Navigation / Account Menu
        JMenuBar mb = new JMenuBar();
        JMenu accountMenu = new JMenu("Account");
        JMenuItem logout = new JMenuItem("Logout");

        logout.addActionListener(e -> {
            UserSession.clear();                  
            dispose();                            
            new Logout().setVisible(true);   
        });

        accountMenu.add(logout);
        mb.add(accountMenu);
        setJMenuBar(mb);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (UserSession.fullName == null) {
                UserSession.userId = 2;
                UserSession.username = "cashier";
                UserSession.fullName = "Cashier User";
                UserSession.role = "Cashier";
            }
            new Cashier().setVisible(true);
        });
    }
}