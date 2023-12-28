import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProfile extends JDialog{
    private JPanel profilePanel;
    private JLabel profileImage;
    private JLabel usernameProfile;
    private JLabel emailProfile;
    private JLabel phoneProfile;
    private JLabel goToProfile;
    private JTextArea usernameTextField;
    private JButton saveButton;
    private JPasswordField passwordTextField;
    private JTextArea emailTextField;
    private JTextArea phoneTextField;

    public EditProfile(JFrame parent)
    {
        super(parent); // Call the parent constructor which requires a JFrame
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setTitle("Edit your profile");
        setContentPane(profilePanel);
        setMinimumSize(new Dimension(517, 507));
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
            // Handle database connection or query execution errors
            e.printStackTrace();
        }

    }
    private ImageIcon resizeImage(ImageIcon imageIcon)
    {
        // Resize the image
        Image originalImage = imageIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}
