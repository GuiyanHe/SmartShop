package edu.tamu.csce634.smartshop.utils;

import java.util.HashMap;
import java.util.Map;

import edu.tamu.csce634.smartshop.models.Recipe;

public class CartManager {
    private static CartManager instance;
    private Map<String, Integer> recipeQuantities; // Recipe title -> quantity

    private CartManager() {
        recipeQuantities = new HashMap<>();
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addRecipe(String recipeTitle) {
        int currentQuantity = recipeQuantities.getOrDefault(recipeTitle, 0);
        recipeQuantities.put(recipeTitle, currentQuantity + 1);
    }

    public void removeRecipe(String recipeTitle) {
        int currentQuantity = recipeQuantities.getOrDefault(recipeTitle, 0);
        if (currentQuantity > 0) {
            recipeQuantities.put(recipeTitle, currentQuantity - 1);
        }
    }

    public int getQuantity(String recipeTitle) {
        return recipeQuantities.getOrDefault(recipeTitle, 0);
    }

    public boolean isInCart(String recipeTitle) {
        return getQuantity(recipeTitle) > 0;
    }

    public void clearCart() {
        recipeQuantities.clear();
    }
}