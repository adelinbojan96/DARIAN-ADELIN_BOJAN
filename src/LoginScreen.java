import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class LoginScreen extends JDialog {
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JButton loginButton;
    private JPanel loginPanel;
    private JLabel goToRegisterText;
    private boolean loginSuccessful = true;
    public LoginScreen(JFrame parent) {
        super(parent); // Call the parent constructor which requires a JFrame
        setTitle("Log into your account");
        setContentPane(loginPanel);
        setMinimumSize(new Dimension(1056, 738));
        setModal(true);
        setLocationRelativeTo(parent);
        setResizable(false);

        setBackgroundColor(Color.decode("#86D3A0"));
        // Customize the login button
        customizeButton(loginButton);
        // Customization in terms of appearance and number of columns the textFields
        customizeTextField(usernameTextField);
        customizeTextField(passwordTextField);
        loginButton.addActionListener(e -> {
            validateLogin(parent);
            if (loginSuccessful) {
                dispose();
                new AnimalDisplayScreen(null);
            }
        });


        goToRegisterText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                dispose();
                new RegisterScreen(null);
            }
        });
        setVisible(true);
    }
    private void customizeButton(JButton button)
    {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                new EmptyBorder(5, 20, 5, 20) // Adjusted the margin for the button
        ));
        button.setPreferredSize(new Dimension(120, button.getPreferredSize().height)); // Adjusted the width

    }
    private void customizeTextField(JTextField textField) {
        textField.setColumns(20);
        textField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        textField.setFont(new Font("Roboto", Font.PLAIN, 16));
    }
    private void validateLogin(JFrame parent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        // Use db.properties file to access database
        DatabaseManager databaseManager = new DatabaseManager();

        // Assuming we have a connection to the database
        try (Connection connection = databaseManager.getConnection()) {
            // Check if the user exists
            String sqlQuery = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    // User exists and passwords match, login successful
                    User loggedInUser = User.createUser(
                            resultSet.getInt("id_user"),
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getString("email"),
                            resultSet.getString("phone_number"),
                            null
                    );
                    User.setCurrentUser(loggedInUser);

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

    private void setBackgroundColor(Color color) {
        loginPanel.setBackground(color);
    }
}
