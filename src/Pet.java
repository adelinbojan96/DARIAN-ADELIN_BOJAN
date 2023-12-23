import javax.swing.ImageIcon;

public class Pet {
    private String name;
    private String animalType;
    private String breed;
    private int age;
    private byte[] image;

    public Pet(String name, String animalType, String breed, int age, byte[] image) {
        this.name = name;
        this.animalType = animalType;
        this.breed = breed;
        this.age = age;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getAnimalType() {
        return animalType;
    }

    public String getBreed() {
        return breed;
    }

    public int getAge() {
        return age;
    }

    public ImageIcon getImageIcon() {
        return (image != null) ? new ImageIcon(image) : null;
    }
}