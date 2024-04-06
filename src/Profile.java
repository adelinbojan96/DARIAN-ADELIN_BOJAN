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
import java.util.Objects;

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
    private JLabel blueIcon;
    private JLabel yellowIcon;
    private JLabel pinkIcon;
    public int numberOfMessages;

    public Profile(JFrame parent) {
        super(parent); // Call the parent constructor which requires a JFrame
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setTitle("Your current profile");
        setContentPane(profilePanel);
        setMinimumSize(new Dimension(517, 618));
        setModal(true);
        setLocationRelativeTo(parent);

        //create a border for the image and set it if not null
        Border emptyBorder = BorderFactory.createLineBorder(Color.BLACK, 5);
        profileImage.setBorder(emptyBorder);

        //display the image if it exists in the database
        if (User.isLoggedIn()) {
            int currentId = User.getCurrentUser().id();
            //retrieve the image data from the database
            byte[] imageData = retrieveImageDataFromDatabase(currentId);

            //if the image data is not null, update the profileImage
            if (imageData != null)
                profileImage.setIcon(resizeImage(imageData));

            //if messages >=0, display
            int messages = retrieveNumberOfMessagesFromTheDatabase(currentId);
            if (messages >= 0) {
                numberOfMessages = messages;
                messagesProfile.setText("Messages sent: " + messages);
            }
        }

        //add customization for the 2 buttons
        customizeButton(buttonPicture);
        customizeButton(buttonEdit);

        //set user information
        setUserInformation();

        //go back to AnimalDisplayScreen
        goToAnimalDisplayScreen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                dispose();
                new AnimalDisplayScreen(null);
            }
        });
        //set new image from local
        buttonPicture.addActionListener(e -> {
            //display file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(Profile.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                //get the selected file
                File selectedFile = fileChooser.getSelectedFile();

                try {
                    //check if the selected file is an image
                    if (isImageFile(selectedFile)) {
                        //convert the selected image file to a byte array
                        byte[] imageData = Files.readAllBytes(selectedFile.toPath());

                        //update the user's profile image in the UI
                        profileImage.setIcon(resizeImage(imageData));

                        //update the pet's image in the db
                        updateUserImageInDatabase(User.getCurrentUser().id(), imageData, parent);
                    } else {
                        JOptionPane.showMessageDialog(Profile.this, "Invalid file format. Please select an image file.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
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
                dispose();
                new LoginScreen(null);
            }
        });
        buttonEdit.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                EditProfile editProfileDialog = new EditProfile(Profile.this, null);
                editProfileDialog.setVisible(true);
            });
            dispose();
        });
        blueIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                updateColor("Blue", parent);
            }
        });
        yellowIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                updateColor("Yellow", parent);
            }
        });
        pinkIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                updateColor("Pink", parent);
            }
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
                            // image update successful
                            JOptionPane.showMessageDialog(parent, "Image update successful");
                        } else {
                            // no rows were affected, update failed
                            JOptionPane.showMessageDialog(parent, "Image update failed");
                        }
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(parent, "Error trying to update image");
                        e.printStackTrace();
                    }
                } else {
                    // user does not exist
                    JOptionPane.showMessageDialog(parent, "A user with this id does not exist");
                }
            }
        } catch (SQLException e) {
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
    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
    }

    private void setUserInformation() {
        //check if the user is logged in
        if (User.isLoggedIn()) {
            //set user information using database or sample data
            int numberOfLettersPassword = User.getCurrentUser().password().length();
            String maskedPassword = "*".repeat(numberOfLettersPassword);
            usernameProfile.setText("User: " + User.getCurrentUser().username());
            passwordProfile.setText("Password: " + maskedPassword);
            emailProfile.setText("Email: " + User.getCurrentUser().email());
            phoneProfile.setText("Phone number: " + User.getCurrentUser().phone());
            //if not null, we display it
            ImageIcon imageIcon = User.getCurrentUser().getImageIcon();
            if (imageIcon != null) {
                //resize the image
                Image originalImage = imageIcon.getImage();
                Image resizedImage = originalImage.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
                profileImage.setIcon(new ImageIcon(resizedImage));
            }

            //update the user
            User user = User.createUser(User.getCurrentUser().id(), User.getCurrentUser().username(),
                    User.getCurrentUser().password(), User.getCurrentUser().email(),
                    User.getCurrentUser().phone(), retrieveImageDataFromDatabase(User.getCurrentUser().id()));
            User.setCurrentUser(user);
        } else {
            //set default or sample data if the user is not logged in
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
                        //retrieve the image data from the result set
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

    public int retrieveNumberOfMessagesFromTheDatabase(int userId) {
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

    private ImageIcon resizeImage(byte[] imageData) {
        ImageIcon imageIcon = new ImageIcon(imageData);
        Image originalImage = imageIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    private void updateColor(String color, JFrame parent) {
        //here the id will come in our aid
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            //check if the user exists
            String selectQuery = "SELECT color_preferred FROM users WHERE id_user = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, User.getCurrentUser().id());
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        //user is found, update the color
                        String updateQuery = "UPDATE users SET color_preferred = ? WHERE id_user = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            //we check what to update in the database

                            updateStatement.setString(1, color);
                            updateStatement.setInt(2, User.getCurrentUser().id());
                            //execute the update statement
                            int rowsAffected = updateStatement.executeUpdate();

                            if (rowsAffected > 0) {
                                //color update was successful
                                JOptionPane.showMessageDialog(parent, "Edits were successful");
                            } else {
                                //no rows were affected, update failed
                                JOptionPane.showMessageDialog(parent, "Edits have failed");
                            }
                        } catch (SQLException e) {
                            JOptionPane.showMessageDialog(parent, "Error trying to update the details of the user (color)");
                            e.printStackTrace();
                        }
                    } else {
                        //user does not exist
                        JOptionPane.showMessageDialog(parent, "A user with this id does not exist");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
