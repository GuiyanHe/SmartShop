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

import edu.tamu.csce634.smartshop.managers.RecipeManager;
import edu.tamu.csce634.smartshop.models.Recipe;

/**
 * RecipeViewModel - ViewModel for Recipe UI
 * 
 * Now delegates to RecipeManager for all recipe operations.
 * Provides LiveData for UI observation.
 */
public class RecipeViewModel extends ViewModel {

    // Optional text (kept for backward compatibility)
    private final MutableLiveData<String> mText = new MutableLiveData<>("This is Recipe fragment");

    private final MutableLiveData<List<Recipe>> recipes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<String, String>> requiredIngredients = new MutableLiveData<>(new LinkedHashMap<>());
    private final MutableLiveData<RecipeManager.NutritionTotals> nutritionTotals = new MutableLiveData<>();

    public RecipeViewModel() { }

    // region Public API for UI

    public LiveData<String> getText() { return mText; }

    public LiveData<List<Recipe>> getRecipes() { return recipes; }

    public LiveData<Map<String, String>> getRequiredIngredients() { return requiredIngredients; }
    
    public LiveData<RecipeManager.NutritionTotals> getNutritionTotals() { return nutritionTotals; }

    /**
     * Initialize data in ViewModel. Call once from Fragment with a Context.
     * This loads recipes from RecipeManager and computes initial aggregations.
     */
    public void init(@NonNull Context context) {
        RecipeManager manager = RecipeManager.getInstance(context);
        
        // Load recipes from manager
        if (recipes.getValue() == null || recipes.getValue().isEmpty()) {
            recipes.setValue(manager.getAllRecipes());
        }
        
        // Refresh aggregated data
        refreshRequiredIngredients(context);
        refreshNutritionTotals(context);
    }

    /** Add one serving of the given recipe to cart and refresh aggregations. */
    public void addToCart(@NonNull Context context, @NonNull Recipe recipe) {
        RecipeManager.getInstance(context).addRecipe(recipe.getTitle());
        refreshRequiredIngredients(context);
        refreshNutritionTotals(context);
    }

    /** Remove one serving of the given recipe from cart and refresh aggregations. */
    public void removeFromCart(@NonNull Context context, @NonNull Recipe recipe) {
        RecipeManager.getInstance(context).removeRecipe(recipe.getTitle());
        refreshRequiredIngredients(context);
        refreshNutritionTotals(context);
    }

    /** Re-compute required ingredients based on current cart. */
    public void refreshRequiredIngredients(@NonNull Context context) {
        Map<String, String> merged = RecipeManager.getInstance(context).getAllRequiredIngredients();
        requiredIngredients.setValue(merged);
    }
    
    /** Re-compute nutrition totals based on current cart. */
    public void refreshNutritionTotals(@NonNull Context context) {
        RecipeManager.NutritionTotals totals = RecipeManager.getInstance(context).calculateTotalNutrition();
        nutritionTotals.setValue(totals);
    }

    // endregion
}