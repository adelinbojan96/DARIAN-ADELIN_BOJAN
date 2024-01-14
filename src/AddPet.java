import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddPet extends JDialog {
    private JPanel mainPanel;
    private final int id_store;
    private JLabel profileImage;
    private JButton buttonPicture;
    private JTextField petName;
    private JTextField petType;
    private JTextField petBreed;
    private JTextField petAge;
    private JTextField storeYears;
    private JTextArea storeBehaviour;
    private JTextArea storeTreatments;
    private JButton buttonAdd;
    private static byte[] imageData;

    public AddPet(AnimalDisplayScreen animalDisplayScreen, JFrame parent, int id_store)
    {
        super(parent);
        this.id_store = id_store;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Add a pet");
        setContentPane(mainPanel);
        setMinimumSize(new Dimension(565, 780));
        setModal(true);
        setLocationRelativeTo(parent);

        //Add a black border for each field
        storeBehaviour.setBorder(new LineBorder(Color.BLACK));
        storeTreatments.setBorder(new LineBorder(Color.BLACK));
        petName.setBorder(new LineBorder(Color.BLACK));
        petBreed.setBorder(new LineBorder(Color.BLACK));
        petAge.setBorder(new LineBorder(Color.BLACK));
        petType.setBorder(new LineBorder(Color.BLACK));
        storeYears.setBorder(new LineBorder(Color.BLACK));

        //Border for image
        Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 3);
        profileImage.setBorder(BorderFactory.createCompoundBorder(blackBorder, BorderFactory.createEmptyBorder(-1, -1, -1, -1)));
        imageData = null;//Set null at the beginning

        customizeButton(buttonPicture);
        customizeButton(buttonAdd);
        buttonPicture.addActionListener(e -> {
            // Display a file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(AddPet.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                File selectedFile = fileChooser.getSelectedFile();

                try {
                    // Check if the selected file is an image
                    if (isImageFile(selectedFile)) {
                        imageData = Files.readAllBytes(selectedFile.toPath());
                        profileImage.setIcon(resizeImage(imageData));
                    } else {
                        JOptionPane.showMessageDialog(AddPet.this, "Invalid file format. Please select an image file.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AddPet.this, "Error reading the selected image file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonAdd.addActionListener(e -> {
            String name = petName.getText();
            String type = petType.getText();
            String breed = petBreed.getText();
            String ageString = petAge.getText();
            int age = 0;
            int years = 0;
            boolean canUseAge = true;
            boolean canUseYears = true;

            try {
                age = Integer.parseInt(ageString);
            } catch (NumberFormatException eAge) {
                canUseAge = false;
                JOptionPane.showMessageDialog(AddPet.this, "Invalid age. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            String yearsString = storeYears.getText();
            try {
                years = Integer.parseInt(yearsString);
            } catch (NumberFormatException eAge) {
                //Can no longer have history for pet
                canUseYears = false;
            }

            String behaviour = storeBehaviour.getText();
            String medicalInfo = storeTreatments.getText();

            boolean successfullyAdded = false;
            if(name != null && canUseAge && type != null && breed != null)
            {

                if(!behaviour.isEmpty() && canUseYears)
                {
                    //We add in history as well
                    int idHistory = addHistoryInDatabase(parent, years, behaviour, medicalInfo);
                    //Create history first, then we add animal
                    successfullyAdded = addPetInDatabase(parent, name, type, breed, age, idHistory, imageData);
                }
                else
                {
                    JOptionPane.showMessageDialog(parent, "No history added to pet");
                    successfullyAdded = addPetInDatabase(parent, name, type, breed, age, -1, imageData);
                }
            }
            else
                JOptionPane.showMessageDialog(parent, "Please fill at least the Pet Name, Type, Breed and Age");
            if(successfullyAdded)
            {
                //Refresh the animalDisplayScreen
                dispose();
                if(animalDisplayScreen!=null)
                    animalDisplayScreen.dispose();
                new AnimalDisplayScreen(null);
            }
        });
        setVisible(true);
    }

    private int addHistoryInDatabase(JFrame parent, int years, String behaviour, String medicalInfo) {
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            //Create an id from the highest id_history, then add 1.
            String maxIdQuery = "SELECT MAX(id_history) AS highest_id_history FROM pet_history";
            try (PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery)) {
                ResultSet maxIdResultSet = maxIdStatement.executeQuery();

                int highestId = 0;
                if (maxIdResultSet.next()) {
                    highestId = maxIdResultSet.getInt("highest_id_history");
                }
                // Calculate the new id_user for the next user
                int newIdHistory = highestId + 1;
                // Create and execute the SQL query to insert a new pet
                String insertQuery = "INSERT INTO pet_history (id_history, years_in_shop, previous_health_issues, pet_behaviour_desc, previous_medical_treatment_desc) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    // Set the values for the parameters in the query
                    insertStatement.setInt(1, newIdHistory);
                    insertStatement.setInt(2, years);
                    insertStatement.setBoolean(3, !medicalInfo.isEmpty());
                    insertStatement.setString(4, behaviour);
                    if(!medicalInfo.isEmpty())
                        insertStatement.setString(5, medicalInfo);
                    else
                        insertStatement.setString(5, null);
                    // Execute the insert query
                    int rowsAffected = insertStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        // History added successfully
                        return newIdHistory;
                    } else {
                        // History not added, handle the error
                        JOptionPane.showMessageDialog(parent, "Error adding pet to the database.", "Error", JOptionPane.ERROR_MESSAGE);
                        return -1;
                    }
                }
            }
            catch (SQLException e)
            {
                JOptionPane.showMessageDialog(parent, "Could not generate id", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent, "Error connecting to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return -1;
    }

    private boolean addPetInDatabase(JFrame parent, String name, String type, String breed, int age, int idHistory, byte[] image) {

        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            //Create an id
            String maxIdQuery = "SELECT MAX(id_pet) AS highest_id_pet FROM pet";
            try (PreparedStatement maxIdStatement = connection.prepareStatement(maxIdQuery)) {
                ResultSet maxIdResultSet = maxIdStatement.executeQuery();

                int highestId = 0;
                if (maxIdResultSet.next()) {
                    highestId = maxIdResultSet.getInt("highest_id_pet");
                }
                // Calculate the new id_user for the next user
                int newIdPet = highestId + 1;
                // Create and execute the SQL query to insert a new pet
                String insertQuery = "INSERT INTO pet (id_pet, name, animal_type, breed, age, id_store, id_history, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                    // Set the values for the parameters in the query
                    insertStatement.setInt(1, newIdPet);
                    insertStatement.setString(2, name);
                    insertStatement.setString(3, type);
                    insertStatement.setString(4, breed);
                    insertStatement.setInt(5, age);
                    insertStatement.setInt(6, this.id_store);//Foreign Key
                    if(idHistory != -1)
                        insertStatement.setInt(7, idHistory);//Foreign Key
                    else
                        insertStatement.setNull(7, java.sql.Types.INTEGER);
                    insertStatement.setBytes(8, image);

                    // Execute the insert query
                    int rowsAffected = insertStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        // Pet added successfully
                        JOptionPane.showMessageDialog(parent, "Pet added successfully!");
                        // If successful, then we will return true
                        return true;
                    } else {
                        // Pet not added, handle the error
                        JOptionPane.showMessageDialog(parent, "Error adding pet to the database.", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            }
            catch (SQLException e)
            {
                JOptionPane.showMessageDialog(parent, "Could not generate id", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } catch (SQLException e) {
            // Handle any SQL-related errors
            JOptionPane.showMessageDialog(parent, "Error connecting to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

        }
        return false;
    }
    // Method to check if the selected file is an image
    private boolean isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
    }
    private ImageIcon resizeImage(byte[] imageData) {
        // Resize the image
        ImageIcon imageIcon = new ImageIcon(imageData);
        Image originalImage = imageIcon.getImage();
        Image resizedImage = originalImage.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
    private void customizeButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 1, true),
                new EmptyBorder(5, 20, 5, 20)
        ));
    }
}
