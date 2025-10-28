package edu.tamu.csce634.smartshop.ui.recipedetail;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

public class RecipeDetailFragment extends Fragment {

    private Recipe recipe;
    private int portionCount = 1;
    private TextView textPortion;
    private Handler handler = new Handler(Looper.getMainLooper());

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

        // Setup long press for portion decrease button
        setupLongPressDecrease(btnDecreasePortion);

        // Setup long press for portion increase button
        setupLongPressIncrease(btnIncreasePortion);

        // Add to cart button
        addToCartBtn.setOnClickListener(v -> {
            if (recipe != null) {
                // Success haptic feedback
                HapticFeedback.success(requireContext());
                
                // Add the recipe multiple times based on portion count
                for (int i = 0; i < portionCount; i++) {
                    CartManager.getInstance(requireContext()).addRecipe(recipe.getTitle());
                }
                
                String message = portionCount == 1 
                    ? recipe.getTitle() + " added to cart!"
                    : portionCount + " portions of " + recipe.getTitle() + " added to cart!";
                    
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                
                // Navigate back to recipe list
                NavHostFragment.findNavController(this).navigateUp();
            }
        });

        textPortion.setOnClickListener(v -> {
            showPortionInputDialog(v);
        });


        return root;
    }

    private void setupLongPressDecrease(ImageButton button) {
        final Runnable[] runnable = new Runnable[1];
        
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Single click
                    if (portionCount > 1) {
                        HapticFeedback.lightClick(v);
                        portionCount--;
                        updatePortionDisplay();
                    }
                    
                    // Start continuous decrement after delay
                    runnable[0] = new Runnable() {
                        @Override
                        public void run() {
                            if (portionCount > 1) {
                                HapticFeedback.lightClick(v);
                                portionCount--;
                                updatePortionDisplay();
                                handler.postDelayed(this, 150); // Repeat every 150ms
                            } else {
                                handler.removeCallbacks(this);
                            }
                        }
                    };
                    handler.postDelayed(runnable[0], 500); // Start after 500ms hold
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Stop continuous decrement
                    if (runnable[0] != null) {
                        handler.removeCallbacks(runnable[0]);
                    }
                    v.performClick();
                    return true;
            }
            return false;
        });
    }

    private void setupLongPressIncrease(ImageButton button) {
        final Runnable[] runnable = new Runnable[1];
        
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Single click
                    if (portionCount < 10) {
                        HapticFeedback.lightClick(v);
                        portionCount++;
                        updatePortionDisplay();
                    }
                    
                    // Start continuous increment after delay
                    runnable[0] = new Runnable() {
                        @Override
                        public void run() {
                            if (portionCount < 10) { // Max 10 portions
                                HapticFeedback.lightClick(v);
                                portionCount++;
                                updatePortionDisplay();
                                handler.postDelayed(this, 150); // Repeat every 150ms
                            } else {
                                handler.removeCallbacks(this);
                            }
                        }
                    };
                    handler.postDelayed(runnable[0], 500); // Start after 500ms hold
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Stop continuous increment
                    if (runnable[0] != null) {
                        handler.removeCallbacks(runnable[0]);
                    }
                    v.performClick();
                    return true;
            }
            return false;
        });
    }

    private void updatePortionDisplay() {
        textPortion.setText(String.valueOf(portionCount));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Clean up handler callbacks
        handler.removeCallbacksAndMessages(null);
        
        // Reset status bar to transparent when leaving this fragment
        if (getActivity() != null && getActivity().getWindow() != null) {
            Window window = getActivity().getWindow();
            window.setStatusBarColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        }
    }

    private void showPortionInputDialog(View view) {
        HapticFeedback.lightClick(view);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set Portions");
        
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(portionCount));
        input.setSelection(input.getText().length());
        builder.setView(input);
        
        builder.setPositiveButton("OK", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                try {
                    int newPortion = Integer.parseInt(text);
                    if (newPortion >= 1 && newPortion <= 10) {
                        portionCount = newPortion;
                        HapticFeedback.mediumClick(view);
                        updatePortionDisplay();
                    } else {
                        Toast.makeText(requireContext(), "Please enter a number between 1 and 10", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}