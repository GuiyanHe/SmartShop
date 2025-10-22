package edu.tamu.csce634.smartshop.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.Recipe;

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
        
        holder.addButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("recipe", recipe);
            Navigation.findNavController(v).navigate(
                R.id.action_recipe_to_detail, bundle
            );
        });
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

        RecipeViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.recipe_image);
            title = itemView.findViewById(R.id.recipe_title);
            description = itemView.findViewById(R.id.recipe_description);
            addButton = itemView.findViewById(R.id.btn_add);
        }
    }
}