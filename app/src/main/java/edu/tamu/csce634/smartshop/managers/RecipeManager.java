package edu.tamu.csce634.smartshop.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.Ingredient;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.utils.QuantityParser;

/**
 * RecipeManager - Centralized manager for all recipe-related operations
 * 
 * Features:
 * - Maintains a catalog of available recipes with nutritional data
 * - Manages recipe cart (quantities of each recipe selected)
 * - Calculates aggregated ingredient requirements
 * - Calculates total nutritional values for cart
 * - Persists cart data using SharedPreferences
 * 
 * Thread-safe: All operations are synchronized
 */
public class RecipeManager {
    private static RecipeManager instance;
    
    // Recipe catalog
    private List<Recipe> availableRecipes;
    
    // Cart data: Recipe title -> quantity
    private Map<String, Integer> recipeCart;
    
    // Persistence
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private static final String PREFS_NAME = "SmartShopRecipes";
    private static final String KEY_CART = "recipe_cart_data";
    
    private RecipeManager(Context context) {
        availableRecipes = new ArrayList<>();
        recipeCart = new HashMap<>();
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        
        initializeRecipes();
        loadCart();
    }
    
    public static synchronized RecipeManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeManager(context);
        }
        return instance;
    }
    
    // ==================== Recipe Catalog ====================
    
    /**
     * Initialize the recipe catalog with sample recipes including nutrition data
     */
    private void initializeRecipes() {
        availableRecipes = new ArrayList<>();
        
        // Tofu Power Bowl
        Recipe tofuBowl = new Recipe(
                "Tofu Power Bowl",
                "High fiber, low calorie, and filling",
                "A vibrant, fresh, and high-protein Tofu Power Bowl. This meal features a centerpiece of savory, seasoned tofu cubes, complemented by a medley of colorful, crisp raw vegetables, and a source of healthy fat and additional protein from hard-boiled eggs and edamame.",
                R.drawable.avocado_salad
        );
        tofuBowl.addIngredient(new Ingredient("Tofu", "2 Oz", R.drawable.tofu));
        tofuBowl.addIngredient(new Ingredient("Brown Rice", "1 Oz", R.drawable.brown_rice));
        tofuBowl.addIngredient(new Ingredient("Egg", "1", R.drawable.egg));
        tofuBowl.addIngredient(new Ingredient("Corn", "2 Oz", R.drawable.corn));
        // Nutrition per serving
        tofuBowl.setNutrition(380, 22, 12, 0); // calories, protein(g), fat(g), water(cups)
        availableRecipes.add(tofuBowl);
        
        // Quinoa Vegetable Stir-fry
        Recipe quinoaStirFry = new Recipe(
                "Quinoa Vegetable Stir-fry",
                "Balanced nutrition, rich in plant protein",
                "A colorful and nutritious quinoa vegetable stir-fry packed with fresh vegetables and plant-based protein. This wholesome dish combines fluffy quinoa with crisp bell peppers, chickpeas, and fresh herbs for a satisfying meal.",
                R.drawable.quinoa_stir_fry
        );
        quinoaStirFry.addIngredient(new Ingredient("Quinoa", "2 Oz", R.drawable.quinoa));
        quinoaStirFry.addIngredient(new Ingredient("Bell Peppers", "1 Oz", R.drawable.bell_pepper));
        quinoaStirFry.addIngredient(new Ingredient("Chickpeas", "2 Oz", R.drawable.chickpea));
        quinoaStirFry.addIngredient(new Ingredient("Cherry Tomatoes", "1 Oz", R.drawable.cherry_tomatos));
        quinoaStirFry.addIngredient(new Ingredient("Red Cabbage", "1 Oz", R.drawable.red_cabbage));
        quinoaStirFry.setNutrition(420, 18, 14, 0);
        availableRecipes.add(quinoaStirFry);
        
        // Salmon Rice Bowl
        Recipe salmonBowl = new Recipe(
                "Salmon Rice Bowl",
                "Flavorful fish, Asian-style",
                "A delicious and healthy salmon rice bowl featuring perfectly cooked salmon slices on a bed of seasoned brown rice. Topped with fresh vegetables including cucumber, radish, carrots, and cilantro, this Asian-inspired dish is both nutritious and satisfying.",
                R.drawable.salmon_rice_bowl
        );
        salmonBowl.addIngredient(new Ingredient("Salmon", "4 Oz", R.drawable.salmon));
        salmonBowl.addIngredient(new Ingredient("Brown Rice", "2 Oz", R.drawable.brown_rice));
        salmonBowl.addIngredient(new Ingredient("Cucumber", "1 Oz", R.drawable.cucumber));
        salmonBowl.addIngredient(new Ingredient("Radish", "0.5 Oz", R.drawable.radish));
        salmonBowl.addIngredient(new Ingredient("Carrots", "1 Oz", R.drawable.carrot));
        salmonBowl.addIngredient(new Ingredient("Cilantro", "0.2 Oz", R.drawable.cilantro));
        salmonBowl.addIngredient(new Ingredient("Egg", "1", R.drawable.egg));
        salmonBowl.setNutrition(520, 32, 18, 0);
        availableRecipes.add(salmonBowl);
        
        // Steak Taco
        Recipe steakTaco = new Recipe(
                "Steak Taco",
                "Mexican dish, low carb",
                "A mouthwatering steak taco featuring tender grilled beef strips on a soft flour tortilla. Loaded with sweet corn, diced tomatoes, fresh cilantro, and crumbled queso fresco cheese. This Mexican-inspired dish delivers bold flavors with a balanced nutritional profile.",
                R.drawable.steak_taco
        );
        steakTaco.addIngredient(new Ingredient("Steak", "4 Oz", R.drawable.steak));
        steakTaco.addIngredient(new Ingredient("Flour Tortilla", "2", R.drawable.flour_tortilla));
        steakTaco.addIngredient(new Ingredient("Corn", "2 Oz", R.drawable.corn));
        steakTaco.addIngredient(new Ingredient("Tomatoes", "1 Oz", R.drawable.cherry_tomatos));
        steakTaco.addIngredient(new Ingredient("Cilantro", "0.2 Oz", R.drawable.cilantro));
        steakTaco.addIngredient(new Ingredient("Queso Fresco", "1 Oz", R.drawable.queso_fresco));
        steakTaco.addIngredient(new Ingredient("Avocado", "1 Oz", R.drawable.avocado));
        steakTaco.setNutrition(480, 28, 22, 0);
        availableRecipes.add(steakTaco);
    }
    
    /**
     * Get all available recipes
     */
    public synchronized List<Recipe> getAllRecipes() {
        return new ArrayList<>(availableRecipes);
    }
    
    /**
     * Get a recipe by title
     */
    public synchronized Recipe getRecipeByTitle(String title) {
        for (Recipe recipe : availableRecipes) {
            if (recipe.getTitle().equals(title)) {
                return recipe;
            }
        }
        return null;
    }
    
    // ==================== Cart Operations ====================
    
    private void loadCart() {
        String json = sharedPreferences.getString(KEY_CART, null);
        if (json != null) {
            Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
            recipeCart = gson.fromJson(json, type);
            if (recipeCart == null) {
                recipeCart = new HashMap<>();
            }
        }
    }
    
    private void saveCart() {
        String json = gson.toJson(recipeCart);
        sharedPreferences.edit().putString(KEY_CART, json).apply();
    }
    
    /**
     * Add one serving of a recipe to cart
     */
    public synchronized void addRecipe(String recipeTitle) {
        int currentQuantity = recipeCart.getOrDefault(recipeTitle, 0);
        recipeCart.put(recipeTitle, currentQuantity + 1);
        saveCart();
    }
    
    /**
     * Remove one serving of a recipe from cart
     */
    public synchronized void removeRecipe(String recipeTitle) {
        int currentQuantity = recipeCart.getOrDefault(recipeTitle, 0);
        if (currentQuantity > 0) {
            recipeCart.put(recipeTitle, currentQuantity - 1);
            if (recipeCart.get(recipeTitle) == 0) {
                recipeCart.remove(recipeTitle);
            }
            saveCart();
        }
    }
    
    /**
     * Get quantity of a specific recipe in cart
     */
    public synchronized int getQuantity(String recipeTitle) {
        return recipeCart.getOrDefault(recipeTitle, 0);
    }
    
    /**
     * Check if recipe is in cart
     */
    public synchronized boolean isInCart(String recipeTitle) {
        return getQuantity(recipeTitle) > 0;
    }
    
    /**
     * Get all items in cart
     */
    public synchronized Map<String, Integer> getAllCartItems() {
        return new HashMap<>(recipeCart);
    }
    
    /**
     * Clear all items from cart
     */
    public synchronized void clearCart() {
        recipeCart.clear();
        saveCart();
    }
    
    /**
     * Get total number of recipe servings in cart
     */
    public synchronized int getTotalCartItems() {
        int total = 0;
        for (int quantity : recipeCart.values()) {
            total += quantity;
        }
        return total;
    }
    
    // ==================== Ingredient Aggregation ====================
    
    /**
     * Get all required ingredients aggregated from cart
     * Returns Map of ingredient name -> total quantity string
     */
    public synchronized Map<String, String> getAllRequiredIngredients() {
        Map<String, Double> numericTotals = new HashMap<>();
        Map<String, String> unitByKey = new HashMap<>();
        Map<String, Integer> fallbackCounts = new HashMap<>();
        Map<String, String> fallbackBaseQty = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : recipeCart.entrySet()) {
            String recipeTitle = entry.getKey();
            int recipeCount = entry.getValue();
            if (recipeCount <= 0) continue;
            
            Recipe recipe = getRecipeByTitle(recipeTitle);
            if (recipe == null) continue;
            
            for (Ingredient ing : recipe.getIngredients()) {
                if (ing == null || ing.getName() == null) continue;
                String ingName = ing.getName().trim();
                String qtyStr = ing.getQuantity() == null ? "" : ing.getQuantity().trim();
                
                QuantityParser.ParsedQuantity pq = QuantityParser.parse(qtyStr);
                if (pq.success) {
                    String key = ingName + "|" + pq.unit;
                    double total = numericTotals.getOrDefault(key, 0.0);
                    total += pq.value * recipeCount;
                    numericTotals.put(key, total);
                    unitByKey.put(key, pq.unit);
                } else {
                    int count = fallbackCounts.getOrDefault(ingName, 0);
                    fallbackCounts.put(ingName, count + recipeCount);
                    fallbackBaseQty.putIfAbsent(ingName, qtyStr.isEmpty() ? "1" : qtyStr);
                }
            }
        }
        
        // Build final result
        Map<String, String> result = new LinkedHashMap<>();
        
        // Numeric totals
        Map<String, Double> byNameTotal = new HashMap<>();
        Map<String, String> unitByName = new HashMap<>();
        for (Map.Entry<String, Double> e : numericTotals.entrySet()) {
            String[] parts = e.getKey().split("\\|", 2);
            String name = parts[0];
            String unit = unitByKey.getOrDefault(e.getKey(), "");
            
            double sum = byNameTotal.getOrDefault(name, 0.0);
            sum += e.getValue();
            byNameTotal.put(name, sum);
            unitByName.putIfAbsent(name, unit);
        }
        
        for (Map.Entry<String, Double> e : byNameTotal.entrySet()) {
            String name = e.getKey();
            String unit = unitByName.getOrDefault(name, "").trim();
            double v = e.getValue();
            String qtyOut = QuantityParser.formatValue(v) + (unit.isEmpty() ? "" : (" " + unit));
            result.put(name, qtyOut);
        }
        
        // Fallbacks
        for (Map.Entry<String, Integer> e : fallbackCounts.entrySet()) {
            String name = e.getKey();
            String base = fallbackBaseQty.getOrDefault(name, "1");
            String fallback = e.getValue() + " × " + base;
            if (result.containsKey(name)) {
                result.put(name, result.get(name) + " (plus " + fallback + ")");
            } else {
                result.put(name, fallback);
            }
        }
        
        return result;
    }
    
    // ==================== Nutrition Calculation ====================
    
    /**
     * Nutrition data holder
     */
    public static class NutritionTotals {
        public int calories;
        public int protein;  // grams
        public int fat;      // grams
        public int water;    // cups
        
        public NutritionTotals(int calories, int protein, int fat, int water) {
            this.calories = calories;
            this.protein = protein;
            this.fat = fat;
            this.water = water;
        }
    }
    
    /**
     * Calculate total nutrition values for all recipes in cart
     * Aligns with the four categories in HomeFragment:
     * - Calories
     * - Protein (g)
     * - Fat (g)
     * - Water (cups)
     */
    public synchronized NutritionTotals calculateTotalNutrition() {
        int totalCalories = 0;
        int totalProtein = 0;
        int totalFat = 0;
        int totalWater = 0;
        
        for (Map.Entry<String, Integer> entry : recipeCart.entrySet()) {
            String recipeTitle = entry.getKey();
            int quantity = entry.getValue();
            
            Recipe recipe = getRecipeByTitle(recipeTitle);
            if (recipe != null) {
                totalCalories += recipe.getCalories() * quantity;
                totalProtein += recipe.getProtein() * quantity;
                totalFat += recipe.getFat() * quantity;
                totalWater += recipe.getWater() * quantity;
            }
        }
        
        return new NutritionTotals(totalCalories, totalProtein, totalFat, totalWater);
    }
}
