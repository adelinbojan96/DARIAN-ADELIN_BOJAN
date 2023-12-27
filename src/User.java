public class User {
    private static User currentUser;  // static field to store the current user
    private int id;

    private String username;
    private String password;
    private String email;
    private String phone;

    // private constructor to prevent direct instantiation
    public User(int id, String username, String password, String email, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

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

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
    // Factory method to create a new user instance
    public static User createUser(int id, String username, String password, String email, String phone) {
        return new User(id, username, password, email, phone);
    }
}
