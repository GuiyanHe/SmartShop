package edu.tamu.csce634.smartshop.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {
    private String title;
    private String description;
    private String fullDescription;
    private int imageResId;
    private List<Ingredient> ingredients;
    
    // Nutrition data (per serving)
    private int calories;    // kcal
    private int protein;     // grams
    private int fat;         // grams
    private int water;       // cups

    public Recipe(String title, String description, String fullDescription, int imageResId) {
        this.title = title;
        this.description = description;
        this.fullDescription = fullDescription;
        this.imageResId = imageResId;
        this.ingredients = new ArrayList<>();
        // Default nutrition values
        this.calories = 0;
        this.protein = 0;
        this.fat = 0;
        this.water = 0;
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFullDescription() { return fullDescription; }
    public int getImageResId() { return imageResId; }
    public List<Ingredient> getIngredients() { return ingredients; }
    
    // Nutrition getters
    public int getCalories() { return calories; }
    public int getProtein() { return protein; }
    public int getFat() { return fat; }
    public int getWater() { return water; }
    
    // Nutrition setters
    public void setNutrition(int calories, int protein, int fat, int water) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.water = water;
    }
    
    public void setCalories(int calories) { this.calories = calories; }
    public void setProtein(int protein) { this.protein = protein; }
    public void setFat(int fat) { this.fat = fat; }
    public void setWater(int water) { this.water = water; }
}