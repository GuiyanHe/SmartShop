package edu.tamu.csce634.smartshop.adapters;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.managers.RecipeManager;
import edu.tamu.csce634.smartshop.utils.HapticFeedback;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    public interface OnCartChangedListener { void onCartChanged(); }
    private OnCartChangedListener cartChangedListener;

    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.cartChangedListener = listener;
    }

    private void notifyCartChanged() {
        if (cartChangedListener != null) cartChangedListener.onCartChanged();
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
            RecipeManager.getInstance(v.getContext()).addRecipe(recipe.getTitle());
            updateButtonState(holder, recipe);
            notifyCartChanged();
        });

        // Click on quantity to manually input
        holder.textQuantity.setOnClickListener(v -> {
            showQuantityInputDialog(v, recipe, holder);
        });

        // Setup long press for increase button
        setupLongPressIncrease(holder, recipe);

        // Setup long press for decrease button
        setupLongPressDecrease(holder, recipe);
    }

    private void showQuantityInputDialog(View view, Recipe recipe, RecipeViewHolder holder) {
        HapticFeedback.lightClick(view);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Set Quantity");
        
        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(RecipeManager.getInstance(view.getContext()).getQuantity(recipe.getTitle())));
        input.setSelection(input.getText().length());
        builder.setView(input);
        
        builder.setPositiveButton("OK", (dialog, which) -> {
            String text = input.getText().toString();
            if (!text.isEmpty()) {
                try {
                    int newQuantity = Integer.parseInt(text);
                    if (newQuantity >= 0 && newQuantity <= 99) {
                        // Clear current quantity
                        int currentQuantity = RecipeManager.getInstance(view.getContext()).getQuantity(recipe.getTitle());
                        for (int i = 0; i < currentQuantity; i++) {
                            RecipeManager.getInstance(view.getContext()).removeRecipe(recipe.getTitle());
                        }
                        // Set new quantity
                        for (int i = 0; i < newQuantity; i++) {
                            RecipeManager.getInstance(view.getContext()).addRecipe(recipe.getTitle());
                        }
                        HapticFeedback.mediumClick(view);
                        updateButtonState(holder, recipe);
                        notifyCartChanged();
                    } else {
                        Toast.makeText(view.getContext(), "Please enter a number between 0 and 99", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(view.getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupLongPressIncrease(RecipeViewHolder holder, Recipe recipe) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable[] runnable = new Runnable[1];
        
        holder.btnIncrease.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Single click
                    HapticFeedback.lightClick(v);
                    RecipeManager.getInstance(v.getContext()).addRecipe(recipe.getTitle());
                    updateButtonState(holder, recipe);
                    notifyCartChanged();
                    
                    // Start continuous increment after delay
                    runnable[0] = new Runnable() {
                        @Override
                        public void run() {
                            HapticFeedback.lightClick(v);
                            RecipeManager.getInstance(v.getContext()).addRecipe(recipe.getTitle());
                            updateButtonState(holder, recipe);
                            notifyCartChanged();
                            handler.postDelayed(this, 150);
                        }
                    };
                    handler.postDelayed(runnable[0], 500);
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
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
                    RecipeManager.getInstance(v.getContext()).removeRecipe(recipe.getTitle());
                    updateButtonState(holder, recipe);
                    notifyCartChanged();
                    
                    // Start continuous decrement after delay
                    runnable[0] = new Runnable() {
                        @Override
                        public void run() {
                            if (RecipeManager.getInstance(v.getContext()).getQuantity(recipe.getTitle()) > 0) {
                                HapticFeedback.lightClick(v);
                                RecipeManager.getInstance(v.getContext()).removeRecipe(recipe.getTitle());
                                updateButtonState(holder, recipe);
                                notifyCartChanged();
                                handler.postDelayed(this, 150);
                            } else {
                                handler.removeCallbacks(this);
                            }
                        }
                    };
                    handler.postDelayed(runnable[0], 500);
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
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
        int quantity = RecipeManager.getInstance(holder.itemView.getContext()).getQuantity(recipe.getTitle());
        
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

    public Recipe getRecipeAt(int position) {
        return recipes.get(position);
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
        View foregroundCard;

        RecipeViewHolder(View itemView) {
            super(itemView);
            foregroundCard = itemView.findViewById(R.id.foreground_card);
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