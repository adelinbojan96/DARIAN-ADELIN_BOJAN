import java.util.ArrayList;
import java.util.List;
import java.sql.*;
public class Adoption {
    private List<Pet> availablePets;

    public Adoption() {
        this.availablePets = new ArrayList<>();
    }

    public void addPet(Pet pet) {
        availablePets.add(pet);
    }
    public void displayAvailablePets() {
        System.out.println("Available Pets:");
        for (Pet pet : availablePets) {
            System.out.println(pet);
        }
    }

    public Pet adoptPet(String petName) {
        for (Pet pet : availablePets) {
            if (pet.getName().equals(petName)) {
                availablePets.remove(pet);
                return pet;
            }
        }
        return null; // Pet not found
    }
}
