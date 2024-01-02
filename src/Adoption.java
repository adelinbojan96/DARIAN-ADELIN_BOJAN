import java.util.ArrayList;
import java.util.List;
public class Adoption {
    private final List<Pet> availablePets;

    public Adoption() {
        this.availablePets = new ArrayList<>();
    }

    public void addPet(Pet pet) {
        availablePets.add(pet);
    }
    public void displayAvailablePets() {
        System.out.println("Available Pets:");
        for (Pet pet : availablePets) {
            System.out.println(pet.getId() + " " + pet.getName() + " " + pet.getAnimalType() +
                    " " + pet.getBreed() + " " + pet.getAge());
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
