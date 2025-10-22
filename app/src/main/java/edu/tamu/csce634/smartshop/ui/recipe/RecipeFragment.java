package edu.tamu.csce634.smartshop.ui.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.adapters.RecipeAdapter;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.models.Ingredient;

public class RecipeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recipe, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<Recipe> recipes = createSampleRecipes();
        RecipeAdapter adapter = new RecipeAdapter(recipes);
        recyclerView.setAdapter(adapter);

        return root;
    }

    private List<Recipe> createSampleRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        Recipe tofuBowl = new Recipe(
            "Tofu Power Bowl",
            "High fiber, low calorie, and filling",
            "A vibrant, fresh, and high-protein Tofu Power Bowl. This meal features a centerpiece of savory, seasoned tofu cubes, complemented by a medley of colorful, crisp raw vegetables, and a source of healthy fat and additional protein from hard-boiled eggs and edamame.",
            R.drawable.avocado_salad
        );
        tofuBowl.addIngredient(new Ingredient("Tofu", "2 Oz", R.drawable.avocado_salad));
        tofuBowl.addIngredient(new Ingredient("Brown Rice", "1 Oz", R.drawable.avocado_salad));
        tofuBowl.addIngredient(new Ingredient("Egg", "1", R.drawable.avocado_salad));
        tofuBowl.addIngredient(new Ingredient("Corn", "2 Oz", R.drawable.avocado_salad));
        tofuBowl.addIngredient(new Ingredient("Purple Cabbage", "1 Oz", R.drawable.avocado_salad));
        recipes.add(tofuBowl);
        
        Recipe quinoaStirFry = new Recipe(
            "Quinoa Vegetable Stir-fry",
            "Balanced nutrition, rich in plant protein",
            "A colorful and nutritious quinoa vegetable stir-fry packed with fresh vegetables and plant-based protein.",
            R.drawable.quinoa_stir_fry
        );
        quinoaStirFry.addIngredient(new Ingredient("Quinoa", "2 Oz", R.drawable.quinoa_stir_fry));
        quinoaStirFry.addIngredient(new Ingredient("Mixed Vegetables", "3 Oz", R.drawable.quinoa_stir_fry));
        recipes.add(quinoaStirFry);
        
        return recipes;
    }
}