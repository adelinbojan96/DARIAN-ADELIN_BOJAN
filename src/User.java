import javax.swing.*;

public record User(int id, String username, String password, String email, String phone, byte[] image) {
    private static User currentUser;  
    

    
    public static User getCurrentUser() {
        return currentUser;
    }

    
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    
    public static void logout() {
        currentUser = null;
    }

    
    public ImageIcon getImageIcon() {
        return (image != null) ? new ImageIcon(image) : null;
    }

    @Override
    public byte[] image() {
        return image;
    }



    public static User createUser(int id, String username, String password, String email, String phone, byte[] image) {
        return new User(id, username, password, email, phone, image);
    }
}
