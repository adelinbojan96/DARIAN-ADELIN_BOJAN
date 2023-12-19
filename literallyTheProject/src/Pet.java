

class Pet {
    private String name;
    private String animal;
    private String breed;
    private int age;

    public Pet(String name, String animal, String breed, int age) {
        this.name = name;
        this.animal = animal;
        this.breed = breed;
        this.age = age;
    }

    public String getName() {
        return name;
    }
    public String getAnimal()
    {
        return animal;
    }

    public String getBreed() {
        return breed;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "name='" + name + '\'' +
                ", animal=" + animal +
                ", breed='" + breed + '\'' +
                ", age=" + age +
                '}';
    }
}