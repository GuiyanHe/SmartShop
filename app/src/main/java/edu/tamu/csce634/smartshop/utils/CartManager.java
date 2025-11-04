package edu.tamu.csce634.smartshop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

import edu.tamu.csce634.smartshop.models.Ingredient;
import edu.tamu.csce634.smartshop.models.Recipe;

public class CartManager {
    private static CartManager instance;
    private Map<String, Integer> recipeQuantities;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private static final String PREFS_NAME = "SmartShopCart";
    private static final String KEY_CART = "cart_data";

    private CartManager(Context context) {
        recipeQuantities = new HashMap<>();
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCart();
    }

    public static CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    // For backward compatibility (deprecated)
    @Deprecated
    public static CartManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CartManager must be initialized with Context first");
        }
        return instance;
    }

    private void loadCart() {
        String json = sharedPreferences.getString(KEY_CART, null);
        if (json != null) {
            Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
            recipeQuantities = gson.fromJson(json, type);
            if (recipeQuantities == null) {
                recipeQuantities = new HashMap<>();
            }
        }
    }

    private void saveCart() {
        String json = gson.toJson(recipeQuantities);
        sharedPreferences.edit().putString(KEY_CART, json).apply();
    }

    public void addRecipe(String recipeTitle) {
        int currentQuantity = recipeQuantities.getOrDefault(recipeTitle, 0);
        recipeQuantities.put(recipeTitle, currentQuantity + 1);
        saveCart();
    }

    public void removeRecipe(String recipeTitle) {
        int currentQuantity = recipeQuantities.getOrDefault(recipeTitle, 0);
        if (currentQuantity > 0) {
            recipeQuantities.put(recipeTitle, currentQuantity - 1);
            if (recipeQuantities.get(recipeTitle) == 0) {
                recipeQuantities.remove(recipeTitle);
            }
            saveCart();
        }
    }

    public int getQuantity(String recipeTitle) {
        return recipeQuantities.getOrDefault(recipeTitle, 0);
    }

    public boolean isInCart(String recipeTitle) {
        return getQuantity(recipeTitle) > 0;
    }

    public Map<String, Integer> getAllItems() {
        return new HashMap<>(recipeQuantities);
    }

    public void clearCart() {
        recipeQuantities.clear();
        saveCart();
    }

    public int getTotalItems() {
        int total = 0;
        for (int quantity : recipeQuantities.values()) {
            total += quantity;
        }
        return total;
    }

    /**
     * Returns a merged view of all required ingredients across all recipes currently in the cart.
     * Contract:
     * - Input: a list of available Recipe objects (title must match the keys used in the cart)
     * - Output: Map<IngredientName, TotalQuantityString>
     * - If quantity string can be parsed as a number + unit (e.g., "2 Oz", "0.5 Oz", "1"),
     *   it will multiply by the recipe quantity and sum values that share the same ingredient name and unit.
     * - If parsing fails, it will fall back to a human-friendly format like "3 x 1 bag" and concatenate sums.
     */
    public Map<String, String> getAllRequiredIngredients(java.util.List<Recipe> allRecipes) {
        if (allRecipes == null) allRecipes = new ArrayList<>();

        // Build lookup by title for quick access
        Map<String, Recipe> recipeByTitle = new HashMap<>();
        for (Recipe r : allRecipes) {
            if (r != null && r.getTitle() != null) {
                recipeByTitle.put(r.getTitle(), r);
            }
        }

        // Accumulate by (ingredient name, unit) => total numeric value
        Map<String, Double> numericTotals = new HashMap<>();
        Map<String, String> unitByKey = new HashMap<>();
        // Track non-numeric fallbacks by ingredient name
        Map<String, Integer> fallbackCounts = new HashMap<>();
        Map<String, String> fallbackBaseQty = new HashMap<>();

        for (Map.Entry<String, Integer> entry : recipeQuantities.entrySet()) {
            String recipeTitle = entry.getKey();
            int recipeCount = entry.getValue();
            if (recipeCount <= 0) continue;
            Recipe recipe = recipeByTitle.get(recipeTitle);
            if (recipe == null) continue; // recipe data not available

            for (Ingredient ing : recipe.getIngredients()) {
                if (ing == null || ing.getName() == null) continue;
                String ingName = ing.getName().trim();
                String qtyStr = ing.getQuantity() == null ? "" : ing.getQuantity().trim();

                ParsedQuantity pq = parseQuantity(qtyStr);
                if (pq.parsed) {
                    String key = ingName + "|" + pq.unit; // unit-aware key
                    double total = numericTotals.getOrDefault(key, 0.0);
                    total += pq.value * recipeCount;
                    numericTotals.put(key, total);
                    unitByKey.put(key, pq.unit);
                } else {
                    // fallback: track count of servings and keep the base quantity string
                    int count = fallbackCounts.getOrDefault(ingName, 0);
                    fallbackCounts.put(ingName, count + recipeCount);
                    // store the first seen base qty string as the descriptor
                    fallbackBaseQty.putIfAbsent(ingName, qtyStr.isEmpty() ? "1" : qtyStr);
                }
            }
        }

        // Build final readable map, grouped by ingredient name
        Map<String, String> result = new LinkedHashMap<>();

        // First, render numeric totals (unit-aware)
        Map<String, Double> byNameTotal = new HashMap<>();
        Map<String, String> unitByName = new HashMap<>();
        for (Map.Entry<String, Double> e : numericTotals.entrySet()) {
            String[] parts = e.getKey().split("\\|", 2);
            String name = parts[0];
            String unit = unitByKey.getOrDefault(e.getKey(), "");

            double sum = byNameTotal.getOrDefault(name, 0.0);
            sum += e.getValue();
            byNameTotal.put(name, sum);
            // Prefer the first non-empty unit
            unitByName.putIfAbsent(name, unit);
        }
        for (Map.Entry<String, Double> e : byNameTotal.entrySet()) {
            String name = e.getKey();
            String unit = unitByName.getOrDefault(name, "").trim();
            double v = e.getValue();
            String qtyOut = formatQuantity(v) + (unit.isEmpty() ? "" : (" " + unit));
            result.put(name, qtyOut);
        }

        // Then, add fallbacks where we couldn't parse numerically
        for (Map.Entry<String, Integer> e : fallbackCounts.entrySet()) {
            String name = e.getKey();
            // If already present from numeric side, append note; else set
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

    private static class ParsedQuantity {
        final boolean parsed;
        final double value;
        final String unit;
        ParsedQuantity(boolean parsed, double value, String unit) {
            this.parsed = parsed;
            this.value = value;
            this.unit = unit;
        }
    }

    // Accepts formats like "2", "2 Oz", "0.5 Oz", "1 pc"; returns parsed number and unit
    private ParsedQuantity parseQuantity(String s) {
        if (s == null) return new ParsedQuantity(false, 0, "");
        String str = s.trim();
        if (str.isEmpty()) return new ParsedQuantity(false, 0, "");
        try {
            // Try simple numeric
            double v = Double.parseDouble(str);
            return new ParsedQuantity(true, v, "");
        } catch (NumberFormatException ignored) {
        }

        // Try pattern: number then unit (split by first space)
        int sp = str.indexOf(' ');
        if (sp > 0) {
            String num = str.substring(0, sp).trim();
            String unit = str.substring(sp + 1).trim();
            try {
                double v = Double.parseDouble(num);
                return new ParsedQuantity(true, v, unit);
            } catch (NumberFormatException ignored) {
            }
        }
        return new ParsedQuantity(false, 0, "");
    }

    private String formatQuantity(double v) {
        // Strip trailing .0 when possible
        if (Math.abs(v - Math.rint(v)) < 1e-9) {
            return String.valueOf((long) Math.rint(v));
        }
        // round to 2 decimals
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}