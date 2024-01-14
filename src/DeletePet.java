import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.Objects;
public class DeletePet extends JDialog {
    private final JPanel mainPanel;
    public DeletePet(JFrame parent, Color backgroundColor, int id_store) {
        super(parent, "Delete Pet from Database", true);
        // When user exists, current frame is gone, and they navigate to AnimalDisplayScreen again.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                new AnimalDisplayScreen(null);
            }
        });

        // Create a main panel with GridBagLayout
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        JScrollPane scrollPane = new JScrollPane(mainPanel);

        // Add the scroll pane to the content pane
        getContentPane().add(scrollPane);

        // Set size and make the dialog visible
        setSize(1280, 720);
        setLocationRelativeTo(parent);

        // Retrieve data from the database and display it
        displayAnimalData(parent, id_store);
        setVisible(true);
    }
    private void displayAnimalData(JFrame parent, int id_store) {
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT id_pet, name, animal_type, breed, age, image FROM pet WHERE id_store = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id_store);
                try (ResultSet resultSet = statement.executeQuery()) {
                    int animalCount = 0;

                    while (resultSet.next()) {
                        int id = resultSet.getInt("id_pet");
                        String name = resultSet.getString("name");
                        String animalType = resultSet.getString("animal_type");
                        String breed = resultSet.getString("breed");
                        int age = resultSet.getInt("age");
                        byte[] imageData = resultSet.getBytes("image");

                        Pet pet = new Pet(id, name, animalType, breed, age, imageData);
                        DeletePet.AnimalPanel animalPanel = new DeletePet.AnimalPanel(parent, pet);
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.gridx = animalCount % 3;  // Set to 3 for 3 columns per row
                        gbc.gridy = animalCount / 3;  // Set to 3 for 3 columns per row
                        gbc.insets = new Insets(10, 10, 10, 10); // Meant for spacing
                        mainPanel.add(animalPanel, gbc);

                        animalCount++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving data from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private class AnimalPanel extends JPanel {
        private static final int PANEL_WIDTH = 400;
        private static final int PANEL_HEIGHT = 183;

        public AnimalPanel(JFrame parent, Pet pet) {
            String name = pet.getName();
            String animalType = pet.getAnimalType();
            String breed = pet.getBreed();
            int age = pet.getAge();

            setLayout(new BorderLayout()); // Main layout uses BorderLayout

            Color backgroundColor = Color.white;
            // Left panel for image
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            leftPanel.setBackground(backgroundColor);

            ImageIcon placeholderIcon = createPlaceholderIcon(); // Create a placeholder ImageIcon

            ImageIcon imageIcon = pet.getImageIcon();
            Image scaledImage = (imageIcon != null) ?
                    imageIcon.getImage().getScaledInstance(160, 160, Image.SCALE_AREA_AVERAGING) :
                    placeholderIcon.getImage().getScaledInstance(160, 160, Image.SCALE_AREA_AVERAGING);

            ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
            JLabel imageLabel = new JLabel(scaledImageIcon);
            Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 3); // Created thickness
            imageLabel.setBorder(BorderFactory.createCompoundBorder(blackBorder, BorderFactory.createEmptyBorder(-1, -1, -1, -1))); // Add an empty border for better framing
            leftPanel.add(imageLabel);



            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Deletes a pet from db
                    deletePet(parent, pet);
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

            rightPanel.add(detailsPanel);
            // Add left and right panels to the main panel
            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);

            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        }
        private ImageIcon createPlaceholderIcon() {
            //Create a placeholder image
            return new ImageIcon(Objects.requireNonNull(getClass().getResource("./Pictures/placeHolder.png")));
        }
    }
    private int getIdHistoryFromDatabase(JFrame parent, Pet pet)
    {
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            // Check if the user exists
            String selectQuery = "SELECT * FROM pet_history WHERE id_history = (SELECT id_history FROM pet WHERE id_pet = ?)";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, pet.getId());
                ResultSet resultSet = selectStatement.executeQuery();
                if(resultSet.next()) {
                    // id found, return it
                    return resultSet.getInt("id_history");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Error trying to access the database");
            e.printStackTrace();
        }
        return -1;
    }
    private void deletePet(JFrame parent, Pet pet)
    {
        int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete " + pet.getName() + " from database?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if(option == JOptionPane.YES_OPTION)
        {
            DatabaseManager databaseManager = new DatabaseManager();
            try (Connection connection = databaseManager.getConnection()) {
                int idHistory = getIdHistoryFromDatabase(parent, pet);//if -1 there is nothing to delete
                String query1 = "DELETE FROM pet WHERE id_pet = ?";
                String query2 = "DELETE FROM pet_history where id_history = ?";
                boolean firstQuery = false;
                boolean secondQuery = false;
                try (PreparedStatement preparedStatement = connection.prepareStatement(query1)) {
                    preparedStatement.setInt(1, pet.getId());
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        firstQuery = true;
                    } else {
                        // Could not delete the rows
                        JOptionPane.showMessageDialog(parent, "Could not delete the rows for the pet");
                    }
                } catch(SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parent, "Could not delete the rows from pet table");
                }
                if(idHistory != -1)
                {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query2)) {
                        preparedStatement.setInt(1, idHistory);
                        int rowsAffected = preparedStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            secondQuery = true;
                        } else {
                            // Could not delete the rows
                            JOptionPane.showMessageDialog(parent, "Could not delete the rows from pet's history");
                        }
                    } catch(SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(parent, "Could not delete the rows from pet's history");
                    }
                }
                else
                {
                    /// There is nothing to delete
                    secondQuery = true;
                }
                if(firstQuery && secondQuery)
                {
                    // Successfully deleted user
                    JOptionPane.showMessageDialog(parent, "Pet deleted successfully");
                    // Access AnimalDisplayScreen
                    dispose();
                    new AnimalDisplayScreen(null);
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
}
