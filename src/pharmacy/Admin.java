package pharmacy;

import AdminContent.*;
import javax.swing.*;
import java.awt.*;

public class Admin extends JFrame {

    public Admin() {
        setTitle("HealthFirst Pharmacy - Administrator Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);

        // Header Panel 
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 153, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel header = new JLabel("Welcome, " + (UserSession.fullName != null ? UserSession.fullName : "Administrator") + " | Administrator Module", SwingConstants.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        header.setForeground(Color.WHITE);
        headerPanel.add(header, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Admin Tabbed Navigation
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 13));
        tabs.addTab("Medicine Management", new Medicine());
        tabs.addTab("Supplier Management", new Supplier());
        tabs.addTab("User Management", new User());
        tabs.addTab("Reports", new Report());

        add(tabs, BorderLayout.CENTER);

        //  Account Menu
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
                UserSession.userId = 1;
                UserSession.username = "admin";
                UserSession.fullName = "Administrator";
                UserSession.role = "Admin";
            }
            new Admin().setVisible(true);
        });
    }
}