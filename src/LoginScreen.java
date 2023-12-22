import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginScreen extends JDialog {
    private JLabel icon;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JLabel username;
    private JLabel password;
    private JButton loginButton;
    private JLabel loginScreenText;
    private JPanel loginPanel;
    private boolean loginSuccessful = true;
    public LoginScreen(JFrame parent) {
        super(parent); // Call the parent constructor which requires a JFrame
        setTitle("Enter a user account");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(1050, 600));
        setModal(true);
        setLocationRelativeTo(parent);

        // Customize the login button
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(Color.BLACK);
        loginButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1, true), // Thin black border
                new EmptyBorder(5, 50, 5, 50)          // Adjust the margin as needed
        ));
        loginButton.setPreferredSize(new Dimension(200, loginButton.getPreferredSize().height)); // Width

        loginButton.addActionListener(e -> {
            validateLogin(parent);
            if (loginSuccessful) {
                // System.out.print("Here I am trying to get to another window");
                // Go to new tab.
                // Create a separate class for this, where I use class message, class user, and class Pet.
                dispose();
                new AnimalDisplayScreen(null);
            }
        });

        // Move setVisible(true) to the end
        setVisible(true);
    }

    public void validateLogin(JFrame parent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        // Assuming we have a connection to the database
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pet", "postgres", "Bojanadelin11!")) {
            // Check if the user exists
            String sqlQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    // User exists and passwords match, login successful
                    JOptionPane.showMessageDialog(parent, "You have successfully logged in.");
                    loginSuccessful = true;
                } else {
                    // User does not exist or passwords do not match
                    JOptionPane.showMessageDialog(parent, "Invalid username or password.");
                    loginSuccessful = false;
                }
            }
        } catch (SQLException e) {
            loginSuccessful = false;
            e.printStackTrace();
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        icon.setSize(50, 50);
    }
}
