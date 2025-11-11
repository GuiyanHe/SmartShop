package edu.tamu.csce634.smartshop.models;

public class ProfileData {
    private String name;
    private String email;
    private int height; // in cm
    private int weight; // in kg
    private String goal;
    private String store;

    // Default constructor for loading
    public ProfileData(String name, String email, int height, int weight, String goal, String store) {
        this.name = name;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.goal = goal;
        this.store = store;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public String getGoal() { return goal; }
    public String getStore() { return store; }

    // Default values for a new user
    public static ProfileData createDefault() {
        return new ProfileData("User", "user@email.com", 175, 75, "Maintain", "XX Supermarket");
    }
}
