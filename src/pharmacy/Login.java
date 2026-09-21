package pharmacy;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Login extends JFrame {

    private JTextField userField = new JTextField();
    private JPasswordField passField = new JPasswordField();

    public Login() {
        setTitle("HealthFirst Pharmacy - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 340);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header Banner in Blue
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 153, 255));
        headerPanel.setPreferredSize(new Dimension(480, 60));
        JLabel title = new JLabel("HealthFirst Pharmacy", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Body
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;
        JLabel userLbl = new JLabel("Username:");
        userLbl.setFont(new Font("Arial", Font.BOLD, 13));
        p.add(userLbl, g);

        g.gridx = 1;
        userField.setPreferredSize(new Dimension(180, 28));
        p.add(userField, g);

        g.gridx = 0; g.gridy++;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Arial", Font.BOLD, 13));
        p.add(passLbl, g);

        g.gridx = 1;
        passField.setPreferredSize(new Dimension(180, 28));
        p.add(passField, g);

        JButton login = new JButton("Login");
        JButton clear = new JButton("Clear");

        // Styling Buttons
        login.setBackground(new Color(0, 153, 255));
        login.setForeground(Color.WHITE);
        login.setFocusPainted(false);
        login.setFont(new Font("Arial", Font.BOLD, 13));

        clear.setBackground(new Color(220, 220, 220));
        clear.setFocusPainted(false);

        JPanel bp = new JPanel();
        bp.setOpaque(false);
        bp.add(login);
        bp.add(clear);

        g.gridx = 0; g.gridy++; g.gridwidth = 2;
        p.add(bp, g);

        mainPanel.add(p, BorderLayout.CENTER);
        add(mainPanel);

        login.addActionListener(e -> login());
        clear.addActionListener(e -> {
            userField.setText("");
            passField.setText("");
        });
        passField.addActionListener(e -> login());
    }

    private void login() {
        String u = userField.getText().trim();
        String pw = new String(passField.getPassword());

        if (u.isEmpty() || pw.isEmpty()) {
            msg("Please enter both username and password.");
            return;
        }

        String sql = "SELECT user_id, username, full_name, role FROM users WHERE username=? AND password=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, pw);

            try (ResultSet r = ps.executeQuery()) {
                if (!r.next()) {
                    msg("Invalid username or password.");
                    return;
                }

                UserSession.userId = r.getInt(1);
                UserSession.username = r.getString(2);
                UserSession.fullName = r.getString(3);
                UserSession.role = r.getString(4);

                if ("Admin".equalsIgnoreCase(UserSession.role)) {
                    new Admin().setVisible(true);
                } else {
                    new Cashier().setVisible(true);
                }
                dispose();
            }
        } catch (SQLException ex) {
            msg("Database error: " + ex.getMessage());
        }
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s, "HealthFirst Pharmacy", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}