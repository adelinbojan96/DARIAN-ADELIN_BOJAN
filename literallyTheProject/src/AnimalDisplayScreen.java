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
                    gbc.insets = new Insets(5, 5, 5, 5); // Add spacing
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

            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            if (imageIcon != null) {
                // Set the size of the image to2 200x200 or greater value
                Image scaledImage = imageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_AREA_AVERAGING);
                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                JLabel imageLabel = new JLabel(scaledImageIcon);

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
            detailsPanel.add(nameLabel);
            detailsPanel.add(typeLabel);
            detailsPanel.add(breedLabel);
            detailsPanel.add(ageLabel);

            commentArea = new JTextArea(2, 20); // Smaller comment area
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            JScrollPane commentScrollPane = new JScrollPane(commentArea);

            JButton saveButton = new JButton("Save Comment");
            saveButton.addActionListener(e -> {
                String comment = commentArea.getText();
                System.out.println("Comment for " + name + ": " + comment);
                JOptionPane.showMessageDialog(null, "Comment saved!");
            });

            add(detailsPanel);
            add(commentScrollPane);
            add(saveButton);
        }
    }
}
