import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.Objects;

public class AnimalDisplayScreen extends JDialog {
    private JPanel mainPanel;
    private JPanel profile;
    private JPanel profilePanel;  // New panel for user information

    public AnimalDisplayScreen(JFrame parent) {
        super(parent, "Animal Display", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create a main panel with GridBagLayout
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.decode("#87abff"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        JScrollPane scrollPane = new JScrollPane(mainPanel);

        // Add the scroll pane to the content pane
        getContentPane().add(scrollPane);

        // Create the panel for user information
        profilePanel = new JPanel();
        profilePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        profilePanel.setBackground(Color.decode("#87abff"));

        // Create labels to display user information
        JLabel nameLabel = new JLabel("user: " + (User.isLoggedIn() ? User.getCurrentUser().username() : "guest"));
        Font labelFont = new Font("Roboto Thin", Font.PLAIN, 15);
        nameLabel.setFont(labelFont);
        profilePanel.add(nameLabel);

        //Action listener for accessing profile
        nameLabel.addMouseListener(new MouseAdapter() {
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

        // Make an invisible border
        Border emptyBorder = BorderFactory.createLineBorder(Color.decode("#87abff"),5);
        profilePanel.setBorder(emptyBorder);
        // Add the user information panel to the content pane
        getContentPane().add(profilePanel, BorderLayout.NORTH);

        // Set size and make the dialog visible
        setSize(1240, 720);
        setLocationRelativeTo(parent);

        // Retrieve data from the database and display it
        displayAnimalData();
        setVisible(true);
    }
    private void displayAnimalData() {
        // JDBC connection parameters (unchanged)
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            // Create and execute the SQL query
            String query = "SELECT id_pet, name, animal_type, breed, age, image FROM pet";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                int animalCount = 0;

                // Iterate through the result set and add a custom AnimalPanel for each entry
                while (resultSet.next()) {
                    int id = resultSet.getInt("id_pet");
                    String name = resultSet.getString("name");
                    String animalType = resultSet.getString("animal_type");
                    String breed = resultSet.getString("breed");
                    int age = resultSet.getInt("age");
                    byte[] imageData = resultSet.getBytes("image");

                    Pet pet = new Pet(id, name, animalType, breed, age, imageData);
                    AnimalPanel animalPanel = new AnimalPanel(pet);
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = animalCount % 3;  // Set to 3 for 3 columns per row
                    gbc.gridy = animalCount / 3;  // Set to 3 for 3 columns per row
                    gbc.insets = new Insets(10, 10, 10, 10); // Increase spacing
                    mainPanel.add(animalPanel, gbc);

                    animalCount++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving data from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private class AnimalPanel extends JPanel {

        public AnimalPanel(Pet pet) {
            String name = pet.getName();
            String animalType = pet.getAnimalType();
            String breed = pet.getBreed();
            int age = pet.getAge();

            setLayout(new BorderLayout()); // Use BorderLayout for the main layout

            Color backgroundColor = Color.white;
            // Left panel for image
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            leftPanel.setBackground(backgroundColor);

            ImageIcon placeholderIcon = createPlaceholderIcon(); // Create a placeholder ImageIcon

            ImageIcon imageIcon = pet.getImageIcon();
            Image scaledImage = (imageIcon != null) ?
                    imageIcon.getImage().getScaledInstance(180, 180, Image.SCALE_AREA_AVERAGING) :
                    placeholderIcon.getImage().getScaledInstance(180, 180, Image.SCALE_AREA_AVERAGING);

            ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
            JLabel imageLabel = new JLabel(scaledImageIcon);
            Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 3); // Created thickness
            imageLabel.setBorder(BorderFactory.createCompoundBorder(blackBorder, BorderFactory.createEmptyBorder(-1, -1, -1, -1))); // Add an empty border for additional spacing
            leftPanel.add(imageLabel);

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    new InfoPet(pet, null);
                }
            });

            // Right panel for details
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

            // Details panel with GridLayout to display details in independent rows
            JPanel detailsPanel = new JPanel(new GridLayout(0, 1));
            detailsPanel.setBackground(backgroundColor);
            JLabel nameLabel = new JLabel(name);
            JLabel typeLabel = new JLabel("Type: " + animalType);
            JLabel breedLabel = new JLabel("Breed: " + breed);
            JLabel ageLabel = new JLabel("Age: " + age);

            nameLabel.setFont(new Font("Roboto Light", Font.PLAIN, 21));
            typeLabel.setFont(new Font("Roboto Thin", Font.ITALIC, 14));
            breedLabel.setFont(new Font("Roboto Thin", Font.ITALIC, 14));
            ageLabel.setFont(new Font("Roboto Thin", Font.ITALIC, 14));

            detailsPanel.add(nameLabel);
            detailsPanel.add(typeLabel);
            detailsPanel.add(breedLabel);
            detailsPanel.add(ageLabel);

            JTextArea commentArea = new JTextArea(2, 20);
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            commentArea.setFont(new Font("Roboto Light", Font.PLAIN, 11));
            JScrollPane commentScrollPane = new JScrollPane(commentArea);

            JButton saveButton = new JButton("Send Comment");
            saveButton.setBackground(Color.WHITE);
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            saveButton.setFont(new Font("Roboto Light", Font.BOLD, 11));
            saveButton.setPreferredSize(new Dimension(60, 20));

            detailsPanel.add(commentScrollPane);
            detailsPanel.add(saveButton);

            rightPanel.add(detailsPanel);

            saveButton.addActionListener(e -> {
                String commentText = commentArea.getText();
                if (!commentText.isEmpty()) {
                    // Call the insertMessage method to insert the comment into the database
                    insertMessage(commentText, pet, AnimalDisplayScreen.this);
                } else {
                    // Show a message if the comment text is empty
                    JOptionPane.showMessageDialog(AnimalDisplayScreen.this, "Please enter a comment before sending.");
                }
            });
            // Add left and right panels to the main panel
            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);
        }

        private ImageIcon createPlaceholderIcon() {
            // Create a placeholder image
            return new ImageIcon(Objects.requireNonNull(getClass().getResource("./Pictures/placeHolder.png")));
        }
        private void insertMessage(String text, Pet pet, AnimalDisplayScreen parent)
        {
            // Use db.properties file to access database
            DatabaseManager databaseManager = new DatabaseManager();
            // Assuming we have a connection to the database
            try (Connection connection = databaseManager.getConnection()) {
                // Check if the connection is fine

                // Find maxId from comments
                String maxIdQuery = "SELECT MAX(id_comment) AS highest_id_comment FROM comments";
                try (PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery))
                {
                    ResultSet maxIdResultSet = maxIdStatement.executeQuery();
                    int highestId = 0;
                    if (maxIdResultSet.next()) {
                        highestId = maxIdResultSet.getInt("highest_id_comment");
                    }
                    // Calculate the new id_user for the next user
                    int newIdComment = highestId + 1;
                    String insertQuery = "INSERT INTO comments(id_comment, text, date, id_user, id_pet) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {

                        // Get the current timestamp
                        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

                        insertStatement.setInt(1, newIdComment);
                        insertStatement.setString(2, text);
                        insertStatement.setTimestamp(3, currentTimestamp);
                        insertStatement.setInt(4, User.getCurrentUser().id());
                        insertStatement.setInt(5, pet.getId());

                        // Execute the insert query
                        int rowsAffected = insertStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            // User exists and passwords match, login successful
                            JOptionPane.showMessageDialog(parent,"Message sent successfully");
                        }
                        else
                            JOptionPane.showMessageDialog(parent,"Message could not be sent, due to a database issue");
                    }
                }
                catch(SQLException e)
                {
                    // Could not create a new id
                    JOptionPane.showMessageDialog(parent, "Could not send the message due to an id related problem");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error connecting do the database");
                e.printStackTrace();
            }
        }
    }
}