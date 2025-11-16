package edu.tamu.csce634.smartshop.ui.recipe;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.Ingredient;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.manager.CartManager;

public class RecipeViewModel extends ViewModel {

    // Optional text (kept for backward compatibility with any existing bindings)
    private final MutableLiveData<String> mText = new MutableLiveData<>("This is Recipe fragment");

    private final MutableLiveData<List<Recipe>> recipes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<String, String>> requiredIngredients = new MutableLiveData<>(new LinkedHashMap<>());

    public RecipeViewModel() { }

    // region Public API for UI

    public LiveData<String> getText() { return mText; }

    public LiveData<List<Recipe>> getRecipes() { return recipes; }

    public LiveData<Map<String, String>> getRequiredIngredients() { return requiredIngredients; }

    /**
     * Initialize data in ViewModel. Call once from Fragment (e.g., onCreate) with a Context.
     * This populates sample recipes and computes the initial required ingredients from the cart.
     */
    public void init(@NonNull Context context) {
        if (recipes.getValue() == null || recipes.getValue().isEmpty()) {
            recipes.setValue(createSampleRecipes());
        }
        refreshRequiredIngredients(context);
    }

    /** Add one serving of the given recipe to the cart and refresh aggregation. */
    public void addToCart(@NonNull Context context, @NonNull Recipe recipe) {
        CartManager.getInstance(context).addRecipe(recipe.getTitle());
        refreshRequiredIngredients(context);
    }

    /** Remove one serving of the given recipe from the cart and refresh aggregation. */
    public void removeFromCart(@NonNull Context context, @NonNull Recipe recipe) {
        CartManager.getInstance(context).removeRecipe(recipe.getTitle());
        refreshRequiredIngredients(context);
    }

    /** Re-compute required ingredients based on current cart and ViewModel's recipe list. */
    public void refreshRequiredIngredients(@NonNull Context context) {
        List<Recipe> list = recipes.getValue();
        if (list == null) list = new ArrayList<>();
        Map<String, String> merged = CartManager.getInstance(context).getAllRequiredIngredients(list);
        requiredIngredients.setValue(merged);
    }

    // endregion

    // region Sample data (moved here from Fragment to align with MVVM)

    private List<Recipe> createSampleRecipes() {
        List<Recipe> recipes = new ArrayList<>();

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
//    tofuBowl.addIngredient(new Ingredient("Purple Cabbage", "1 Oz", R.drawable.purple_cabbage));
        recipes.add(tofuBowl);

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
        recipes.add(quinoaStirFry);

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
        recipes.add(salmonBowl);

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
        recipes.add(steakTaco);

        return recipes;
    }

    // endregion
}