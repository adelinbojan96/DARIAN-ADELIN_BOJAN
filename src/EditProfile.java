import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProfile extends JDialog{
    private final Profile profile;
    private JPanel profilePanel;
    private JLabel profileImage;
    private JLabel goToProfile;
    private JTextField usernameTextField;
    private JButton saveButton;
    private JPasswordField passwordTextField;
    private JTextField emailTextField;
    private JTextField phoneTextField;
    private JLabel deleteMessages;
    private JLabel deleteUser;

    public EditProfile(Profile profile, JFrame parent)
    {
        super(parent);
        this.profile = profile;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setTitle("Edit your profile");
        setContentPane(profilePanel);
        setMinimumSize(new Dimension(517, 544));
        setModal(true);
        setLocationRelativeTo(parent);

        Border emptyBorder = BorderFactory.createLineBorder(Color.BLACK, 5);
        profileImage.setBorder(emptyBorder);

        // Display the image if it exists in the database and is updated in the User Class
        if(User.isLoggedIn() && User.getCurrentUser() != null)
        {
            ImageIcon imageIcon = User.getCurrentUser().getImageIcon();
            if(imageIcon != null)
                profileImage.setIcon(resizeImage(imageIcon));
        }
        customizeButton(saveButton);
        saveButton.addActionListener(e -> {
            if(User.isLoggedIn() && User.getCurrentUser() != null)
            {
                String newUsername = usernameTextField.getText();
                String newPassword = passwordTextField.getText();
                String newEmail = emailTextField.getText();
                String newPhone = phoneTextField.getText();
                updateUserSettingsInDatabase(User.getCurrentUser().id(), newUsername, newPassword, newEmail, newPhone, parent);
            }
        });
        goToProfile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                SwingUtilities.invokeLater(() -> {
                    Profile profileDialog = new Profile(null);
                    profileDialog.setVisible(true);
                });
                dispose();
            }
        });
        deleteMessages.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                deleteMessages(parent, User.getCurrentUser().id());
            }
        });
        deleteUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                deleteUser(parent, User.getCurrentUser().id());
            }
        });
    }

    private void updateUserSettingsInDatabase(int id, String newUsername, String newPassword, String newEmail, String newPhone, JFrame parent)
    {
        //Here the id will come in our aid
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            // Check if the user exists
            String selectQuery = "SELECT * FROM users WHERE id_user = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, id);
                ResultSet resultSet = selectStatement.executeQuery();
                if (resultSet.next()) {
                    // User is found, update the image
                    String updateQuery = "UPDATE users SET username = ?, password = ?, email = ?, phone_number = ?  WHERE id_user = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        //We check what to update in the database
                        String username = (!newUsername.isEmpty()) ? newUsername : User.getCurrentUser().username();
                        String password = (!newPassword.isEmpty()) ? newPassword : User.getCurrentUser().password();
                        String email = (!newEmail.isEmpty()) ? newEmail : User.getCurrentUser().email();
                        String phone = (!newPhone.isEmpty()) ? newPhone : User.getCurrentUser().phone();

                        updateStatement.setString(1, username);
                        updateStatement.setString(2, password);
                        updateStatement.setString(3, email);
                        updateStatement.setString(4, phone);
                        updateStatement.setInt(5, id);

                        // Execute the update statement
                        int rowsAffected = updateStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            // Image update successful
                            JOptionPane.showMessageDialog(parent, "Edits were successful");
                            User user = User.createUser(id, username, password, email, phone, User.getCurrentUser().image());
                            User.setCurrentUser(user);

                            SwingUtilities.invokeLater(() -> {
                                Profile profileDialog = new Profile(null);
                                profileDialog.setVisible(true);
                            });
                            dispose();

                        } else {
                            // No rows were affected, update failed
                            JOptionPane.showMessageDialog(parent, "Edits have failed");
                        }
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(parent, "Error trying to update the details of the user");
                        e.printStackTrace();
                    }
                } else {
                    // User does not exist
                    JOptionPane.showMessageDialog(parent, "A user with this id does not exist");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void deleteMessages(JFrame parent, int id_user) {

        int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete all messages?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            DatabaseManager databaseManager = new DatabaseManager();
            try (Connection connection = databaseManager.getConnection()) {
                String query = "DELETE FROM comments WHERE id_user = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, id_user);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        // Display message and then go back to Profile Screen
                        JOptionPane.showMessageDialog(parent, "Successfully deleted all the messages");

                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            Profile profileDialog = new Profile(null);
                            profileDialog.setVisible(true);
                        });

                    } else {
                        // Could not delete the rows
                        JOptionPane.showMessageDialog(parent, "Could not delete the rows");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("The deletion could not be performed");
            }
        } else {
            // User could not to delete messages
            JOptionPane.showMessageDialog(parent, "Deletion canceled");
        }
    }

    private void deleteUser(JFrame parent, int id_user)
    {
        // Ask for confirmation before deleting messages
        int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete the user?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if(option == JOptionPane.YES_OPTION)
        {
            DatabaseManager databaseManager = new DatabaseManager();
            try (Connection connection = databaseManager.getConnection()) {
                String query1 = "DELETE FROM comments WHERE id_user = ?";
                String query2 = "DELETE FROM users where id_user = ?";
                boolean firstQuery = false;
                boolean secondQuery = false;
                try (PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
                    preparedStatement.setInt(1, id_user);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0 || profile.numberOfMessages == 0) {
                        firstQuery = true;
                    } else {
                        // Could not delete the rows
                        JOptionPane.showMessageDialog(parent, "Could not delete the rows");
                    }
                }
                try (PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
                    preparedStatement.setInt(1, id_user);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        secondQuery = true;
                    } else {
                        // Could not delete the rows
                        JOptionPane.showMessageDialog(parent, "Could not delete the rows");
                    }
                }
                if(firstQuery && secondQuery)
                {
                    // Successfully deleted user
                    JOptionPane.showMessageDialog(parent, "User deleted successfully");

                    // Logout from record User
                    User.logout();

                    // Access LoginScreen
                    dispose();
                    new LoginScreen(null);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("The deletion could not be performed");
            }
        } else {
            // User could not to delete messages
            JOptionPane.showMessageDialog(parent, "Deletion canceled");
        }
    }
    private void customizeButton(JButton button)
    {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                new EmptyBorder(5, 20, 5, 20)
        ));

    }
    private ImageIcon resizeImage(ImageIcon imageIcon)
    {
        // Resize the image
        Image originalImage = imageIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}
