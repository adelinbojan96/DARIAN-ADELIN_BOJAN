import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InfoPet extends JDialog {
    private JPanel petPanel;
    private JLabel petImage;
    private JLabel animalName;
    private JLabel animalBreed;
    private JLabel animalAge;
    private JLabel animalType;
    private JLabel storeName;
    private JLabel storeYears;
    private JLabel storeCity;
    private JLabel historyYears;
    private JLabel historyBehaviour;
    private JLabel historyIssues;

    public InfoPet(Pet pet, JFrame parent)
    {
        super();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setTitle("Your current profile");
        setContentPane(petPanel);
        setMinimumSize(new Dimension(737, 787));
        setModal(true);
        setLocationRelativeTo(parent);

        animalName.setText("Name: " + pet.getName());
        animalType.setText("Type: " + pet.getAnimalType());
        animalBreed.setText("Breed: " + pet.getBreed());
        animalAge.setText("Age: " + pet.getAge());
        ImageIcon petIcon = pet.getImageIcon();
        if(petIcon != null)
        {
            Image scaledImage = petIcon.getImage().getScaledInstance(240, 220, Image.SCALE_AREA_AVERAGING);
            petImage.setIcon(new ImageIcon(scaledImage));
        }
        // Create a border for the image and set it if not null
        Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 3);
        petImage.setBorder(BorderFactory.createCompoundBorder(lineBorder, BorderFactory.createEmptyBorder(-1, -1, -1, -1)));

        databaseStore(pet, parent);
        databaseHistory(pet, parent);
        setVisible(true);
    }
    private void databaseStore(Pet pet, JFrame parent)
    {
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            // Check if the user exists
            String selectQuery = "SELECT * FROM store WHERE id_store = (SELECT id_store FROM pet WHERE id_pet = ?)";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, pet.getId());
                ResultSet resultSet = selectStatement.executeQuery();
                if(resultSet.next()) {
                    // id found, update the texts with the corresponding values from database
                    storeName.setText("Name: " + resultSet.getString("name"));
                    storeYears.setText("Years of activity: " + resultSet.getInt("years_of_activity"));
                    storeCity.setText("Location: " + resultSet.getString("city"));
                } else {
                    // User does not exist
                    JOptionPane.showMessageDialog(parent, "Could not display the store information due to an id related problem");
                }
            }
        } catch (SQLException e) {
            // Handle database connection or query execution errors
            JOptionPane.showMessageDialog(parent, "Error trying to access the database");
            e.printStackTrace();
        }
    }
    private void databaseHistory(Pet pet, JFrame parent)
    {
        DatabaseManager databaseManager = new DatabaseManager();
        try (Connection connection = databaseManager.getConnection()) {
            // Check if the user exists
            String selectQuery = "SELECT * FROM pet_history WHERE id_history = (SELECT id_history FROM pet WHERE id_pet = ?)";
            try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                selectStatement.setInt(1, pet.getId());
                ResultSet resultSet = selectStatement.executeQuery();
                if(resultSet.next()) {
                    String descriptionIssues = resultSet.getString("previous_medical_treatment_desc");
                    // id found, update the texts with the corresponding values from database
                    historyYears.setText("Years spent in this center: " + resultSet.getInt("years_in_shop"));
                    historyBehaviour.setText("Behaviour: " + resultSet.getString("pet_behaviour_desc"));
                    if(descriptionIssues != null && !descriptionIssues.isEmpty())
                        historyIssues.setText(resultSet.getString("previous_medical_treatment_desc"));
                    else
                        historyIssues.setText("None");
                }
            }
        } catch (SQLException e) {
            // Handle database connection or query execution errors
            JOptionPane.showMessageDialog(parent, "Error trying to access the database");
            e.printStackTrace();
        }
    }
}
