import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AnimalDisplayScreen extends JDialog {
    private JPanel mainPanel;

    public AnimalDisplayScreen(JFrame parent) {
        super(parent, "Animal Display", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create a main panel with GridBagLayout
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        JScrollPane scrollPane = new JScrollPane(mainPanel);

        // Add the scroll pane to the content pane
        getContentPane().add(scrollPane);

        // Set size and make the dialog visible
        setSize(1050, 600);
        setLocationRelativeTo(parent);

        // Retrieve data from the database and display it
        displayAnimalData();

        setVisible(true);
    }

    private void displayAnimalData() {
        // JDBC connection parameters
        String url = "jdbc:postgresql://localhost:5432/pet";
        String username = "postgres";
        String password = "Bojanadelin11!";

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            // Create and execute the SQL query
            String query = "SELECT name, animal_type, breed, age, image FROM pet";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                int animalCount = 0;

                // Iterate through the result set and add a custom AnimalPanel for each entry
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String animalType = resultSet.getString("animal_type");
                    String breed = resultSet.getString("breed");
                    int age = resultSet.getInt("age");
                    byte[] imageData = resultSet.getBytes("image");

                    ImageIcon imageIcon = (imageData != null) ? new ImageIcon(imageData) : null;

                    AnimalPanel animalPanel = new AnimalPanel(name, animalType, breed, age, imageIcon);
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = animalCount % 4;
                    gbc.gridy = animalCount / 4;
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

    private static class AnimalPanel extends JPanel {
        private String name;
        private String animalType;
        private String breed;
        private int age;
        private JTextArea commentArea;

        public AnimalPanel(String name, String animalType, String breed, int age, ImageIcon imageIcon) {
            this.name = name;
            this.animalType = animalType;
            this.breed = breed;
            this.age = age;

            // Use a MatteBorder for thicker borders
            setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            if (imageIcon != null) {
                // Set the size of the image to 200x200 or greater value
                Image scaledImage = imageIcon.getImage().getScaledInstance(180, 180, Image.SCALE_AREA_AVERAGING);
                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                JLabel imageLabel = new JLabel(scaledImageIcon);

                // Create a thin frame around the image
                imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                // Center the image at the top
                JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                imagePanel.add(imageLabel);

                add(imagePanel);
            }

            // Details panel with GridLayout to display details in independent rows
            JPanel detailsPanel = new JPanel(new GridLayout(0, 1));
            JLabel nameLabel = new JLabel("Name: " + name);
            JLabel typeLabel = new JLabel("Type: " + animalType);
            JLabel breedLabel = new JLabel("Breed: " + breed);
            JLabel ageLabel = new JLabel("Age: " + age);

            // Adjust spacing between labels
            nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            typeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            breedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            ageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

            // Use Roboto font for labels with increased font size and center alignment
            Font robotoFont = new Font("Roboto", Font.PLAIN, 18);
            nameLabel.setFont(robotoFont);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            typeLabel.setFont(robotoFont);
            typeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            breedLabel.setFont(robotoFont);
            breedLabel.setHorizontalAlignment(SwingConstants.CENTER);
            ageLabel.setFont(robotoFont);
            ageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            commentArea = new JTextArea(2, 20); // Smaller comment area
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            JScrollPane commentScrollPane = new JScrollPane(commentArea);

            // Create a white rectangular button and center it
            JButton saveButton = new JButton("Save Comment");
            saveButton.setBackground(Color.WHITE);
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            detailsPanel.add(nameLabel);
            detailsPanel.add(typeLabel);
            detailsPanel.add(breedLabel);
            detailsPanel.add(ageLabel);

            detailsPanel.add(commentScrollPane); // Add comment area to detailsPanel
            detailsPanel.add(saveButton); // Add button to detailsPanel

            saveButton.addActionListener(e -> {
                String comment = commentArea.getText();
                System.out.println("Comment for " + name + ": " + comment);
                JOptionPane.showMessageDialog(null, "Comment saved!");
            });

            add(detailsPanel);
        }
    }
}