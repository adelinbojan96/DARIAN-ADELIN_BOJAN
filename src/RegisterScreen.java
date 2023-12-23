import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class RegisterScreen extends JDialog {
    private JPanel registerPanel;
    private JLabel icon;
    private JLabel password;
    private JLabel username;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JButton registerButton;
    private JLabel orangeRegister;
    private JLabel goToLoginText;
    private JTextField mailTextField;
    private JTextField phoneTextField;
    private boolean registerSuccessful = false;
    public RegisterScreen(JFrame parent) {
        super(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Create a new account");
        setContentPane(registerPanel);
        setMinimumSize(new Dimension(1090, 738));
        setModal(true);
        setBackgroundColor(Color.decode("#86D3A0"));
        setLocationRelativeTo(parent);

        // Customize the register button
        customizeButton(registerButton);
        // Customization in terms of appearance and number of columns the textFields
        customizeTextField(usernameTextField);
        customizeTextField(passwordTextField);
        customizeTextField(mailTextField);
        customizeTextField(phoneTextField);

        goToLoginText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                dispose();
                //go to LoginScreen
                new LoginScreen(null);
            }
        });
        registerButton.addActionListener(e -> {
               validateRegister(parent);
               if(registerSuccessful)
               {
                   dispose();
                   new AnimalDisplayScreen(null);
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
    public void validateRegister(JFrame parent)
    {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        String email = mailTextField.getText();
        String number = phoneTextField.getText();
        // Assuming we have a connection to the database
        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pet", "postgres", "Bojanadelin11!")) {
            // Check if the user exists
            String sqlQuery = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    // User exists or passwords match, register failed
                    JOptionPane.showMessageDialog(parent, "A user with the same name already exists");
                    registerSuccessful = false;
                } else {
                    // User does not exist, it is good news
                    if(!password.isEmpty())
                    {
                        String maxIdQuery = "SELECT MAX(id_user) AS highest_id_user FROM users";
                        try (PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery))
                        {
                            ResultSet maxIdResultSet = maxIdStatement.executeQuery();

                            int highestId = 0;
                            if (maxIdResultSet.next()) {
                                highestId = maxIdResultSet.getInt("highest_id_user");
                            }

                            // Calculate the new id_user for the next user
                            int newIdUser = highestId + 1;
                            String insertQuery = "INSERT INTO users(id_user, username, password, email, phone_number) VALUES (?, ?, ?, ?, ?)";
                            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                                insertStatement.setInt(1, newIdUser);
                                insertStatement.setString(2, username);
                                insertStatement.setString(3, password);
                                insertStatement.setString(4, email);
                                insertStatement.setString(5, number);

                                // Execute the insert query
                                int rowsAffected = insertStatement.executeUpdate();

                                //if (rowsAffected > 0) {
                                // Registration successful
                                //registerSuccessful = true;
                                registerSuccessful = rowsAffected > 0;
                            }
                        }
                        catch(SQLException e)
                        {
                            JOptionPane.showMessageDialog(parent, "Could not put the id");
                            registerSuccessful = false;
                        }finally {
                            try {
                                // Close ResultSet and PreparedStatement
                                resultSet.close();
                                preparedStatement.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    else
                    {
                        JOptionPane.showMessageDialog(parent, "Please provide username and password");
                        registerSuccessful = false;
                    }

                }
            }
        } catch (SQLException e) {
            registerSuccessful = false;
            e.printStackTrace();
        }
    }
    private void createUIComponents() {
        icon.setSize(50, 50);
    }
    private void setBackgroundColor(Color color) {
        registerPanel.setBackground(color);
    }
}
