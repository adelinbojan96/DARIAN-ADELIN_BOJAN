import javax.swing.*;

public record User(int id, String username, String password, String email, String phone, byte[] image) {
    private static User currentUser;  // static field to store the current user
    // private constructor to prevent direct instantiation

    // public method to get the current user
    public static User getCurrentUser() {
        return currentUser;
    }

    // public method to set the current user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // public method to check if a user is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // public method to log out the current user
    public static void logout() {
        currentUser = null;
    }

    // Getter methods for user attributes
    public ImageIcon getImageIcon() {
        return (image != null) ? new ImageIcon(image) : null;
    }

    @Override
    public byte[] image() {
        return image;
    }

    // Factory method to create a new user instance
    public static User createUser(int id, String username, String password, String email, String phone, byte[] image) {
        return new User(id, username, password, email, phone, image);
    }
}
