package edu.tamu.csce634.smartshop.models;

public class DailyStats {
    private int currentCalories;
    private int currentProtein;
    private int currentFat;
    private int currentWater; // in cups

    public DailyStats(int currentCalories, int currentProtein, int currentFat, int currentWater) {
        this.currentCalories = currentCalories;
        this.currentProtein = currentProtein;
        this.currentFat = currentFat;
        this.currentWater = currentWater;
    }

    // Getters
    public int getCurrentCalories() { return currentCalories; }
    public int getCurrentProtein() { return currentProtein; }
    public int getCurrentFat() { return currentFat; }
    public int getCurrentWater() { return currentWater; }

    public static DailyStats createDefault() {
        return new DailyStats(0, 0, 0, 0);
    }
}