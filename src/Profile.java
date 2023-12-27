import javax.swing.*;
import java.awt.*;

public class Profile extends JDialog{
    private JLabel profileImage;
    private JLabel usernameProfile;
    private JLabel emailProfile;
    private JLabel phoneProfile;
    private JButton buttonPicture;
    private JButton buttonEdit;
    private JLabel goToAnimalDisplayScreen;
    private JPanel profilePanel;

    public Profile(JFrame parent)
    {
        super(parent); // Call the parent constructor which requires a JFrame
        setTitle("Log into your account");
        setContentPane(profilePanel);
        setMinimumSize(new Dimension(1056, 738));
        setModal(true);
        setLocationRelativeTo(parent);
    }
}
