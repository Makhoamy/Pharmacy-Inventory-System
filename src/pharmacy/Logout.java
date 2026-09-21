package pharmacy;

import javax.swing.*;
import java.awt.*;

public class Logout extends JFrame {
    
    public Logout() {
        setTitle("HealthFirst Pharmacy - Session Ended");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 320);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 153, 255));
        headerPanel.setPreferredSize(new Dimension(480, 60));
        
        JLabel title = new JLabel("HealthFirst Pharmacy", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        headerPanel.add(title);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Body Content
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel("✓", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(0, 153, 255));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msgLabel = new JLabel("You have been successfully logged out.", SwingConstants.CENTER);
        msgLabel.setFont(new Font("Arial", Font.BOLD, 15));
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Thank you for using HealthFirst Pharmacy Management System.", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subLabel.setForeground(Color.GRAY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bodyPanel.add(iconLabel);
        bodyPanel.add(Box.createVerticalStrut(10));
        bodyPanel.add(msgLabel);
        bodyPanel.add(Box.createVerticalStrut(5));
        bodyPanel.add(subLabel);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        btnPanel.setBackground(Color.WHITE);

        JButton loginAgainBtn = new JButton("Log Back In");
        loginAgainBtn.setBackground(new Color(0, 153, 255));
        loginAgainBtn.setForeground(Color.WHITE);
        loginAgainBtn.setFont(new Font("Arial", Font.BOLD, 13));
        loginAgainBtn.setFocusPainted(false);
        loginAgainBtn.setPreferredSize(new Dimension(130, 35));

        JButton exitBtn = new JButton("Exit System");
        exitBtn.setBackground(new Color(220, 220, 220));
        exitBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        exitBtn.setFocusPainted(false);
        exitBtn.setPreferredSize(new Dimension(120, 35));

        btnPanel.add(loginAgainBtn);
        btnPanel.add(exitBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Event Listeners
        loginAgainBtn.addActionListener(e -> {
            dispose();
            new Login().setVisible(true);
        });

        exitBtn.addActionListener(e -> System.exit(0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Logout().setVisible(true));
    }
}
    

