
package AdminContent;

import pharmacy.DatabaseConnection;
import pharmacy.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class User extends JPanel {
   private JTextField idField = new JTextField();
    private JTextField usernameField = new JTextField();
    private JTextField fullNameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Cashier", "Admin"});

    private DefaultTableModel model = new DefaultTableModel(
        new String[]{"User ID", "Username", "Full Name", "Role"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private JTable table = new JTable(model);

    public User() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // FORM PANEL
        JPanel formPanel = new JPanel(new GridLayout(2, 6, 8, 8));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 153, 255), 1), "User Account Details"
        ));

        idField.setEditable(false);

        formPanel.add(new JLabel("ID (Auto):"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleCombo);

        // BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton addBtn = createStyledButton("Create User");
        JButton deleteBtn = createStyledButton("Delete User");
        JButton clearBtn = new JButton("Clear");
        clearBtn.setPreferredSize(new Dimension(100, 32));

        btnPanel.add(addBtn);
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
        loadUsers();

        addBtn.addActionListener(e -> createUser());
        deleteBtn.addActionListener(e -> deleteUser());
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

    private void loadUsers() {
        model.setRowCount(0);
        String sql = "SELECT user_id, username, full_name, role FROM users ORDER BY user_id";
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                model.addRow(new Object[]{
                    r.getInt("user_id"),
                    r.getString("username"),
                    r.getString("full_name"),
                    r.getString("role")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }

    private void createUser() {
        String u = usernameField.getText().trim();
        String full = fullNameField.getText().trim();
        String pw = new String(passwordField.getPassword()).trim();
        String role = (String) roleCombo.getSelectedItem();

        if (u.isEmpty() || full.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        String sql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, u);
            p.setString(2, pw);
            p.setString(3, role);
            p.setString(4, full);

            p.executeUpdate();
            JOptionPane.showMessageDialog(this, "User " + u + " created successfully!");
            loadUsers();
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        String idText = idField.getText().trim();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.");
            return;
        }

        int selectedId = Integer.parseInt(idText);
        if (selectedId == UserSession.userId) {
            JOptionPane.showMessageDialog(this, "You cannot delete the currently logged-in account!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete User #" + selectedId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE user_id = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement p = c.prepareStatement(sql)) {

                p.setInt(1, selectedId);
                p.executeUpdate();
                JOptionPane.showMessageDialog(this, "User deleted successfully!");
                loadUsers();
                clearForm();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }
    }

    private void populateForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;

        idField.setText(String.valueOf(model.getValueAt(r, 0)));
        usernameField.setText(String.valueOf(model.getValueAt(r, 1)));
        fullNameField.setText(String.valueOf(model.getValueAt(r, 2)));
        roleCombo.setSelectedItem(String.valueOf(model.getValueAt(r, 3)));
        passwordField.setText("");
    }

    private void clearForm() {
        idField.setText("");
        usernameField.setText("");
        fullNameField.setText("");
        passwordField.setText("");
        roleCombo.setSelectedIndex(0);
        table.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("User Panel Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1150, 720);
            frame.setLocationRelativeTo(null);
            frame.add(new User());
            frame.setVisible(true);
        });
    }
} 
