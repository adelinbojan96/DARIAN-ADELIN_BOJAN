import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VirtualDisplayPetAdoption implements ActionListener {

    static void demo()
    {
        // Adding some pets to the platform
        //adoptionPlatform.addPet(new Pet("Buddy", "Dog","Labrador", 2));
        //adoptionPlatform.addPet(new Pet("Mittens", "Cat", "Persian", 4));
        //adoptionPlatform.addPet(new Pet("Whiskers", "Cat", "Siamese", 3));
        //adoptionPlatform.addPet(new Pet("Nemo", "Fish", "Clownfish",1));

        // Display available pets
        //adoptionPlatform.displayAvailablePets();
        /*
        // Adopt a pet (it also deletes it)
        Pet adoptedPet = adoptionPlatform.adoptPet("Buddy");
        if (adoptedPet != null) {
            System.out.println("Congratulations! You've adopted: " + adoptedPet);
        } else {
            System.out.println("Sorry, the requested pet is not available for adoption.");
        }
        */
        // Display updated available pets
    }
    public static void main(String[] args) {

        Adoption adoptionPlatform = new Adoption();
        new loginScreen(null);
        //demo();
        adoptionPlatform.displayAvailablePets();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}