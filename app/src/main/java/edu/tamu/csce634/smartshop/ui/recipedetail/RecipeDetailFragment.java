package edu.tamu.csce634.smartshop.ui.recipedetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.adapters.IngredientAdapter;
import edu.tamu.csce634.smartshop.models.Recipe;

public class RecipeDetailFragment extends Fragment {

    private Recipe recipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, 
                             @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        // Get recipe from arguments
        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
        }

        // Setup toolbar
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigateUp()
        );

        // Set recipe data
        TextView title = root.findViewById(R.id.recipe_title);
        ImageView image = root.findViewById(R.id.recipe_image);
        TextView description = root.findViewById(R.id.recipe_description);
        MaterialButton addToCartBtn = root.findViewById(R.id.btn_add_to_cart);

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

        addToCartBtn.setOnClickListener(v -> 
            Toast.makeText(getContext(), "Added to cart!", Toast.LENGTH_SHORT).show()
        );

        return root;
    }
}