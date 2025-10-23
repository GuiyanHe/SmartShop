package edu.tamu.csce634.smartshop.ui.recipedetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.adapters.IngredientAdapter;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.utils.CartManager;
import edu.tamu.csce634.smartshop.utils.HapticFeedback;

public class RecipeDetailFragment extends Fragment {

    private Recipe recipe;
    private int portionCount = 1;
    private TextView textPortion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                             @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        // Set status bar color to match toolbar
        if (getActivity() != null && getActivity().getWindow() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.green_fab));
        }

        // Get recipe from arguments
        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
        }

        // Setup toolbar
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            HapticFeedback.lightClick(v);
            NavHostFragment.findNavController(this).navigateUp();
        });

        // Set recipe data
        TextView title = root.findViewById(R.id.recipe_title);
        ImageView image = root.findViewById(R.id.recipe_image);
        TextView description = root.findViewById(R.id.recipe_description);
        MaterialButton addToCartBtn = root.findViewById(R.id.btn_add_to_cart);

        // Portion selector elements
        textPortion = root.findViewById(R.id.text_portion);
        ImageButton btnDecreasePortion = root.findViewById(R.id.btn_decrease_portion);
        ImageButton btnIncreasePortion = root.findViewById(R.id.btn_increase_portion);

        if (recipe != null) {
            title.setText(recipe.getTitle());
            image.setImageResource(recipe.getImageResId());
            description.setText(recipe.getFullDescription());

            // Setup ingredients RecyclerView
            RecyclerView ingredientsRecyclerView = root.findViewById(R.id.ingredients_recycler_view);
            ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            IngredientAdapter adapter = new IngredientAdapter(recipe.getIngredients());
            ingredientsRecyclerView.setAdapter(adapter);
        }

        // Portion decrease button
        btnDecreasePortion.setOnClickListener(v -> {
            if (portionCount > 1) {
                HapticFeedback.lightClick(v);
                portionCount--;
                updatePortionDisplay();
            }
        });

        // Portion increase button
        btnIncreasePortion.setOnClickListener(v -> {
            if (portionCount < 10) { // Max 10 portions
                HapticFeedback.lightClick(v);
                portionCount++;
                updatePortionDisplay();
            }
        });

        // Add to cart button
        addToCartBtn.setOnClickListener(v -> {
            if (recipe != null) {
                // Success haptic feedback
                HapticFeedback.success(requireContext());
                
                // Add the recipe multiple times based on portion count
                for (int i = 0; i < portionCount; i++) {
                    CartManager.getInstance().addRecipe(recipe.getTitle());
                }
                
                String message = portionCount == 1 
                    ? recipe.getTitle() + " added to cart!"
                    : portionCount + " portions of " + recipe.getTitle() + " added to cart!";
                    
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                
                // Navigate back to recipe list
                NavHostFragment.findNavController(this).navigateUp();
            }
        });

        return root;
    }

    private void updatePortionDisplay() {
        textPortion.setText(String.valueOf(portionCount));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Reset status bar to transparent when leaving this fragment
        if (getActivity() != null && getActivity().getWindow() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        }
    }
}