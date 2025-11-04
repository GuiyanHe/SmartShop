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

    public Recipe(String title, String description, String fullDescription, int imageResId) {
        this.title = title;
        this.description = description;
        this.fullDescription = fullDescription;
        this.imageResId = imageResId;
        this.ingredients = new ArrayList<>();
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFullDescription() { return fullDescription; }
    public int getImageResId() { return imageResId; }
    public List<Ingredient> getIngredients() { return ingredients; }
}