package edu.tamu.csce634.smartshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.Ingredient;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private List<Ingredient> ingredients;

    public IngredientAdapter(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.name.setText(ingredient.getName());
        holder.quantity.setText(ingredient.getQuantity());
        holder.image.setImageResource(ingredient.getImageResId());
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView quantity;

        IngredientViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ingredient_image);
            name = itemView.findViewById(R.id.ingredient_name);
            quantity = itemView.findViewById(R.id.ingredient_quantity);
        }
    }
}