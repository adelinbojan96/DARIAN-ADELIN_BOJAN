import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Profile extends JDialog {
    private JLabel profileImage;
    private JLabel usernameProfile;
    private JLabel emailProfile;
    private JLabel phoneProfile;
    private JButton buttonPicture;
    private JButton buttonEdit;
    private JLabel goToAnimalDisplayScreen;
    private JPanel profilePanel;
    private JLabel goToLoginScreen;
    private JLabel passwordProfile;
    private JLabel messagesProfile;
    public int numberOfMessages;

    public Profile(JFrame parent) {
        super(parent); // Call the parent constructor which requires a JFrame
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setTitle("Your current profile");
        setContentPane(profilePanel);
        setMinimumSize(new Dimension(517, 567));
        setModal(true);
        setLocationRelativeTo(parent);

        // Create a border for the image and set it if not null
        Border emptyBorder = BorderFactory.createLineBorder(Color.BLACK, 5);
        profileImage.setBorder(emptyBorder);

        // Display the image if it exists in the database
        if(User.isLoggedIn())
        {
            int currentId = User.getCurrentUser().id();
            // Retrieve the image data from the database
            byte[] imageData = retrieveImageDataFromDatabase(currentId);

            // If the image data is not null, update the profileImage
            if (imageData != null)
                profileImage.setIcon(resizeImage(imageData));

            // If messages >=0, display
            int messages = retrieveNumberOfMessagesFromTheDatabase(currentId);
            if(messages >= 0)
            {
                numberOfMessages = messages;
                messagesProfile.setText("Messages sent: " + messages);
            }
        }

        // Add customization for the 2 buttons
        customizeButton(buttonPicture);
        customizeButton(buttonEdit);

        // Set user information
        setUserInformation();

        // Go back to AnimalDisplayScreen
        goToAnimalDisplayScreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                new AnimalDisplayScreen(null);
                dispose();
            }
        });
        // Set new image from local
        buttonPicture.addActionListener(e -> {
            // Display a file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(Profile.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                File selectedFile = fileChooser.getSelectedFile();

                try {
                    // Convert the selected image file to a byte array
                    byte[] imageData = Files.readAllBytes(selectedFile.toPath());

                    // Update the user's profile image in the UI
                    profileImage.setIcon(resizeImage(imageData));

                    // Update the pet's image in the database
                    updateUserImageInDatabase(User.getCurrentUser().id(), imageData, parent);

                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(Profile.this, "Error reading the selected image file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        goToLoginScreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                //go to LoginScreen and log out the current user
                User.logout();
                new LoginScreen(null);
                dispose();
            }
        });
        buttonEdit.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                EditProfile editProfileDialog = new EditProfile(Profile.this,null);
                editProfileDialog.setVisible(true);
            });
            dispose();
        });
    }

    private void updateUserImageInDatabase(int id, byte[] imageData, JFrame parent) {
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            // Check if the user exists
            String selectQuery = "SELECT * FROM users WHERE id_user = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, id);
                ResultSet resultSet = selectStatement.executeQuery();
                if (resultSet.next()) {
                    // User is found, update the image
                    String updateQuery = "UPDATE users SET user_image = ? WHERE id_user = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {

                        updateStatement.setBytes(1, imageData);
                        updateStatement.setInt(2, id);

                        // Execute the update statement
                        int rowsAffected = updateStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            // Image update successful
                            JOptionPane.showMessageDialog(parent, "Image update successful");
                        } else {
                            // No rows were affected, update failed
                            JOptionPane.showMessageDialog(parent, "Image update failed");
                        }
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(parent, "Error trying to update image");
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

    private void customizeButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                new EmptyBorder(5, 20, 5, 20)
        ));
    }

    private void setUserInformation() {
        // Check if the user is logged in
        if (User.isLoggedIn()) {
            // Set user information using database or sample data
            int numberOfLettersPassword = User.getCurrentUser().password().length();
            String maskedPassword = "*".repeat(numberOfLettersPassword);
            usernameProfile.setText("User: " + User.getCurrentUser().username());
            passwordProfile.setText("Password: " +  maskedPassword);
            emailProfile.setText("Email: " + User.getCurrentUser().email());
            phoneProfile.setText("Phone number: " + User.getCurrentUser().phone());
            //If not null, we display it
            ImageIcon imageIcon = User.getCurrentUser().getImageIcon();
            if(imageIcon != null)
            {
                // Resize the image
                Image originalImage = imageIcon.getImage();
                Image resizedImage = originalImage.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
                profileImage.setIcon(new ImageIcon(resizedImage));
            }

            // Update the user
            User user = User.createUser(User.getCurrentUser().id(), User.getCurrentUser().username(),
                    User.getCurrentUser().password(), User.getCurrentUser().email(),
                    User.getCurrentUser().phone(), retrieveImageDataFromDatabase(User.getCurrentUser().id()));
            User.setCurrentUser(user);
        } else {
            // Set default or sample data if the user is not logged in
            usernameProfile.setText("User: Guest");
            emailProfile.setText("Email: Email");
            phoneProfile.setText("Phone number: No Phone Number");
        }
    }
    private byte[] retrieveImageDataFromDatabase(int userId) {
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT user_image FROM users WHERE id_user = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve the image data from the result set
                        return resultSet.getBytes("user_image");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The image from the database could not be retrieved");
        }
        return null;
    }
    public int retrieveNumberOfMessagesFromTheDatabase(int userId){
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT COUNT(id_comment) as count_messages FROM comments WHERE id_user = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, userId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve the number of messages from user
                        return resultSet.getInt("count_messages");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("The number of messages from the database could not be retrieved");
        }
        return 0;
    }
    private ImageIcon resizeImage(byte[] imageData)
    {
        // Resize the image
        ImageIcon imageIcon = new ImageIcon(imageData);
        Image originalImage = imageIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

}
