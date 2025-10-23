package edu.tamu.csce634.smartshop.adapters;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.utils.CartManager;
import edu.tamu.csce634.smartshop.utils.HapticFeedback;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;

    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.title.setText(recipe.getTitle());
        holder.description.setText(recipe.getDescription());
        holder.image.setImageResource(recipe.getImageResId());
        
        // Update UI based on cart status
        updateButtonState(holder, recipe);

        // Info button - navigate to detail page
        holder.btnInfo.setOnClickListener(v -> {
            HapticFeedback.lightClick(v);
            Bundle bundle = new Bundle();
            bundle.putSerializable("recipe", recipe);
            Navigation.findNavController(v).navigate(
                R.id.action_recipe_to_detail, bundle
            );
        });

        // Add button - add to cart
        holder.addButton.setOnClickListener(v -> {
            HapticFeedback.mediumClick(v);
            CartManager.getInstance().addRecipe(recipe.getTitle());
            updateButtonState(holder, recipe);
        });

        // Setup long press for increase button
        setupLongPressIncrease(holder, recipe);

        // Setup long press for decrease button
        setupLongPressDecrease(holder, recipe);
    }

    private void setupLongPressIncrease(RecipeViewHolder holder, Recipe recipe) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable[] runnable = new Runnable[1];
        
        holder.btnIncrease.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Single click
                    HapticFeedback.lightClick(v);
                    CartManager.getInstance().addRecipe(recipe.getTitle());
                    updateButtonState(holder, recipe);
                    
                    // Start continuous increment after delay
                    runnable[0] = new Runnable() {
                        @Override
                        public void run() {
                            HapticFeedback.lightClick(v);
                            CartManager.getInstance().addRecipe(recipe.getTitle());
                            updateButtonState(holder, recipe);
                            handler.postDelayed(this, 150); // Repeat every 150ms
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

    private void setupLongPressDecrease(RecipeViewHolder holder, Recipe recipe) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable[] runnable = new Runnable[1];
        
        holder.btnDecrease.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Single click
                    HapticFeedback.lightClick(v);
                    CartManager.getInstance().removeRecipe(recipe.getTitle());
                    updateButtonState(holder, recipe);
                    
                    // Start continuous decrement after delay
                    runnable[0] = new Runnable() {
                        @Override
                        public void run() {
                            if (CartManager.getInstance().getQuantity(recipe.getTitle()) > 0) {
                                HapticFeedback.lightClick(v);
                                CartManager.getInstance().removeRecipe(recipe.getTitle());
                                updateButtonState(holder, recipe);
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

    private void updateButtonState(RecipeViewHolder holder, Recipe recipe) {
        int quantity = CartManager.getInstance().getQuantity(recipe.getTitle());
        
        if (quantity > 0) {
            holder.addButton.setVisibility(View.GONE);
            holder.quantityControls.setVisibility(View.VISIBLE);
            holder.textQuantity.setText(String.valueOf(quantity));
        } else {
            holder.addButton.setVisibility(View.VISIBLE);
            holder.quantityControls.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;
        MaterialButton addButton;
        LinearLayout quantityControls;
        TextView textQuantity;
        ImageButton btnIncrease;
        ImageButton btnDecrease;
        FloatingActionButton btnInfo;

        RecipeViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recipe_image);
            title = itemView.findViewById(R.id.recipe_title);
            description = itemView.findViewById(R.id.recipe_description);
            addButton = itemView.findViewById(R.id.btn_add);
            quantityControls = itemView.findViewById(R.id.quantity_controls);
            textQuantity = itemView.findViewById(R.id.text_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnInfo = itemView.findViewById(R.id.btn_info);
        }
    }
}