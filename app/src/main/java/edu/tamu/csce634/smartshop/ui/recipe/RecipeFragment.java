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
import edu.tamu.csce634.smartshop.models.Ingredient;
import edu.tamu.csce634.smartshop.models.Recipe;

public class RecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recipe, container, false);

        recyclerView = root.findViewById(R.id.recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        List<Recipe> recipes = createSampleRecipes();
        adapter = new RecipeAdapter(recipes);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the adapter when returning to this fragment
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private List<Recipe> createSampleRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        
        // Tofu Power Bowl
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
        
        // Quinoa Vegetable Stir-fry
        Recipe quinoaStirFry = new Recipe(
            "Quinoa Vegetable Stir-fry",
            "Balanced nutrition, rich in plant protein",
            "A colorful and nutritious quinoa vegetable stir-fry packed with fresh vegetables and plant-based protein. This wholesome dish combines fluffy quinoa with crisp bell peppers, chickpeas, and fresh herbs for a satisfying meal.",
            R.drawable.quinoa_stir_fry
        );
        quinoaStirFry.addIngredient(new Ingredient("Quinoa", "2 Oz", R.drawable.quinoa_stir_fry));
        quinoaStirFry.addIngredient(new Ingredient("Bell Peppers", "1 Oz", R.drawable.quinoa_stir_fry));
        quinoaStirFry.addIngredient(new Ingredient("Chickpeas", "2 Oz", R.drawable.quinoa_stir_fry));
        quinoaStirFry.addIngredient(new Ingredient("Cherry Tomatoes", "1 Oz", R.drawable.quinoa_stir_fry));
        quinoaStirFry.addIngredient(new Ingredient("Red Cabbage", "1 Oz", R.drawable.quinoa_stir_fry));
        recipes.add(quinoaStirFry);
        
        // Salmon Rice Bowl
        Recipe salmonBowl = new Recipe(
            "Salmon Rice Bowl",
            "Flavorful fish, Asian-style",
            "A delicious and healthy salmon rice bowl featuring perfectly cooked salmon slices on a bed of seasoned brown rice. Topped with fresh vegetables including cucumber, radish, carrots, and cilantro, this Asian-inspired dish is both nutritious and satisfying.",
            R.drawable.salmon_rice_bowl
        );
        salmonBowl.addIngredient(new Ingredient("Salmon", "4 Oz", R.drawable.salmon_rice_bowl));
        salmonBowl.addIngredient(new Ingredient("Brown Rice", "2 Oz", R.drawable.salmon_rice_bowl));
        salmonBowl.addIngredient(new Ingredient("Cucumber", "1 Oz", R.drawable.salmon_rice_bowl));
        salmonBowl.addIngredient(new Ingredient("Radish", "0.5 Oz", R.drawable.salmon_rice_bowl));
        salmonBowl.addIngredient(new Ingredient("Carrots", "1 Oz", R.drawable.salmon_rice_bowl));
        salmonBowl.addIngredient(new Ingredient("Cilantro", "0.2 Oz", R.drawable.salmon_rice_bowl));
        salmonBowl.addIngredient(new Ingredient("Egg", "1", R.drawable.salmon_rice_bowl));
        recipes.add(salmonBowl);
        
        // Steak Taco
        Recipe steakTaco = new Recipe(
            "Steak Taco",
            "Mexican dish, low carb",
            "A mouthwatering steak taco featuring tender grilled beef strips on a soft flour tortilla. Loaded with sweet corn, diced tomatoes, fresh cilantro, and crumbled queso fresco cheese. This Mexican-inspired dish delivers bold flavors with a balanced nutritional profile.",
            R.drawable.steak_taco
        );
        steakTaco.addIngredient(new Ingredient("Steak", "4 Oz", R.drawable.steak_taco));
        steakTaco.addIngredient(new Ingredient("Flour Tortilla", "2", R.drawable.steak_taco));
        steakTaco.addIngredient(new Ingredient("Corn", "2 Oz", R.drawable.steak_taco));
        steakTaco.addIngredient(new Ingredient("Tomatoes", "1 Oz", R.drawable.steak_taco));
        steakTaco.addIngredient(new Ingredient("Cilantro", "0.2 Oz", R.drawable.steak_taco));
        steakTaco.addIngredient(new Ingredient("Queso Fresco", "1 Oz", R.drawable.steak_taco));
        steakTaco.addIngredient(new Ingredient("Avocado", "1 Oz", R.drawable.steak_taco));
        recipes.add(steakTaco);
        
        return recipes;
    }
}