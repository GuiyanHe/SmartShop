package edu.tamu.csce634.smartshop.models;

public class ProfileData {
    private String name;
    private String email;
    private int height; // in cm
    private int weight; // in kg
    private String goal; // e.g., "Maintain", "Fat Loss"
    private String store;

    // Updated Goals
    private int calorieGoal;
    private int proteinGoal;
    private int fatGoal;
    private int waterGoal; // in cups

    public ProfileData(String name, String email, int height, int weight, String goal, String store,
                       int calorieGoal, int proteinGoal, int fatGoal, int waterGoal) {
        this.name = name;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.goal = goal;
        this.store = store;
        this.calorieGoal = calorieGoal;
        this.proteinGoal = proteinGoal;
        this.fatGoal = fatGoal;
        this.waterGoal = waterGoal;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public String getGoal() { return goal; }
    public String getStore() { return store; }
    public int getCalorieGoal() { return calorieGoal; }
    public int getProteinGoal() { return proteinGoal; }
    public int getFatGoal() { return fatGoal; }
    public int getWaterGoal() { return waterGoal; }

    // Default values
    public static ProfileData createDefault() {
        // Updated defaults
        return new ProfileData("User", "user@email.com", 175, 75, "Maintain", "XX Supermarket",
                2000, 150, 70, 8); // 8 cups of water
    }
}
