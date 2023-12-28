import javax.swing.ImageIcon;

public class Pet {
    private final int id_pet;
    private final String name;
    private final String animalType;
    private final String breed;
    private final int age;
    private final byte[] image;

    public Pet(int id_pet, String name, String animalType, String breed, int age, byte[] image) {
        this.id_pet = id_pet;
        this.name = name;
        this.animalType = animalType;
        this.breed = breed;
        this.age = age;
        this.image = image;
    }

    public int getId(){return id_pet;}
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