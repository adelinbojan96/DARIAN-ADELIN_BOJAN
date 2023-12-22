import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Objects;
public class AnimalDisplayScreen extends JDialog {
    private JPanel mainPanel;

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

        // Set size and make the dialog visible
        setSize(1340, 720);
        setLocationRelativeTo(parent);

        // Retrieve data from the database and display it
        displayAnimalData();

        setVisible(true);
    }
    public void displayAnimalDialog() {
        // Retrieve data from the database and display it
        displayAnimalData();

        // Set the dialog visible
        setVisible(true);
    }
    private void displayAnimalData() {
        // JDBC connection parameters (unchanged)
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

                    //ImageIcon imageIcon = (imageData != null) ? new ImageIcon(imageData) : null;

                    Pet pet = new Pet(name, animalType, breed, age, imageData);
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


    private static class AnimalPanel extends JPanel {

        public AnimalPanel(Pet pet) {
            String name = pet.getName();
            String animalType = pet.getAnimalType();
            String breed = pet.getBreed();
            int age = pet.getAge();

            setLayout(new BorderLayout()); // Use BorderLayout for the main layout

            // Left panel for image
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            leftPanel.setBackground(Color.decode("#fff2eb"));

            ImageIcon placeholderIcon = createPlaceholderIcon(); // Create a placeholder ImageIcon

            ImageIcon imageIcon = pet.getImageIcon();
            Image scaledImage = (imageIcon != null) ? imageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_AREA_AVERAGING) : placeholderIcon.getImage().getScaledInstance(200, 200, Image.SCALE_AREA_AVERAGING);

            ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
            JLabel imageLabel = new JLabel(scaledImageIcon);
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            leftPanel.add(imageLabel);

            // Right panel for details
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBackground(Color.decode("#fff2eb"));

            // Details panel with GridLayout to display details in independent rows
            JPanel detailsPanel = new JPanel(new GridLayout(0, 1));
            JLabel nameLabel = new JLabel("Name: " + name);
            JLabel typeLabel = new JLabel("Type: " + animalType);
            JLabel breedLabel = new JLabel("Breed: " + breed);
            JLabel ageLabel = new JLabel("Age: " + age);

            nameLabel.setFont(new Font("Roboto", Font.PLAIN, 17));
            typeLabel.setFont(new Font("Roboto", Font.PLAIN, 17));
            breedLabel.setFont(new Font("Roboto", Font.PLAIN, 17));
            ageLabel.setFont(new Font("Roboto", Font.PLAIN, 17));

            detailsPanel.add(nameLabel);
            detailsPanel.add(typeLabel);
            detailsPanel.add(breedLabel);
            detailsPanel.add(ageLabel);

            JTextArea commentArea = new JTextArea(2, 20);
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            JScrollPane commentScrollPane = new JScrollPane(commentArea);

            JButton saveButton = new JButton("Send Comment");
            saveButton.setBackground(Color.WHITE);
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            detailsPanel.add(commentScrollPane);
            detailsPanel.add(saveButton);

            rightPanel.add(detailsPanel);

            // Add left and right panels to the main panel
            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);
        }

        private ImageIcon createPlaceholderIcon() {
            // Create a placeholder image (you can customize this image)
            return new ImageIcon(Objects.requireNonNull(getClass().getResource("./placeHolder.png")));
        }

    }
    /*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Animal Display Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            AnimalDisplayScreen animalDisplayScreen = new AnimalDisplayScreen(frame);
            animalDisplayScreen.displayAnimalDialog();

            frame.setVisible(true);
        });
    }
    */
}