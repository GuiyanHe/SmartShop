package edu.tamu.csce634.smartshop.ui.recipe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.adapters.RecipeAdapter;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.manager.RecipeManager;
import edu.tamu.csce634.smartshop.utils.HapticFeedback;
import edu.tamu.csce634.smartshop.utils.SwipeHelper;

public class RecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private SwipeHelper swipeHelper;
    private RecipeViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recipe, container, false);

        recyclerView = root.findViewById(R.id.recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ViewModel wiring
        viewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        viewModel.init(requireContext());

        // Observe recipes and (re)attach adapter when list becomes available
        viewModel.getRecipes().observe(getViewLifecycleOwner(), list -> {
            if (list == null) list = new ArrayList<>();
            adapter = new RecipeAdapter(list);
            adapter.setOnCartChangedListener(() -> viewModel.refreshRequiredIngredients(requireContext()));
            recyclerView.setAdapter(adapter);
        });

        // Show aggregated required ingredients
        View fab = root.findViewById(R.id.fab_show_ingredients);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                viewModel.refreshRequiredIngredients(requireContext());
                java.util.Map<String, String> map = viewModel.getRequiredIngredients().getValue();
                String message;
                if (map == null || map.isEmpty()) {
                    message = "Your cart is empty. Add some recipes to see required ingredients.";
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (java.util.Map.Entry<String, String> e : map.entrySet()) {
                        sb.append("• ").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
                    }
                    message = sb.toString();
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("All Required Ingredients")
                        .setMessage(message)
                        .setPositiveButton("OK", (d, w) -> d.dismiss())
                        .show();
            });
        }

        // Setup swipe to delete
        setupSwipeToDelete();

        // Close swipe when scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (swipeHelper != null && (Math.abs(dx) > 0 || Math.abs(dy) > 0)) {
                    swipeHelper.closeOpenItem(recyclerView);
                }
            }
        });

        return root;
    }

    private void setupSwipeToDelete() {
        swipeHelper = new SwipeHelper(requireContext()) {
            @Override
            public boolean canSwipe(int position) {
                Recipe recipe = adapter.getRecipeAt(position);
                return RecipeManager.getInstance(requireContext()).getQuantity(recipe.getTitle()) > 0;
            }

            @Override
            public void onDeleteClick(int position) {
                Recipe recipe = adapter.getRecipeAt(position);
                showDeleteConfirmation(recipe, position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showDeleteConfirmation(Recipe recipe, int position) {
    int quantity = RecipeManager.getInstance(requireContext()).getQuantity(recipe.getTitle());
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Remove from Cart");
        builder.setMessage("Remove " + quantity + (quantity == 1 ? " portion" : " portions") + 
                          " of " + recipe.getTitle() + " from cart?");
        
        builder.setPositiveButton("Remove", (dialog, which) -> {
            HapticFeedback.mediumClick(recyclerView);
            
            // Remove all quantities
            for (int i = 0; i < quantity; i++) {
                RecipeManager.getInstance(requireContext()).removeRecipe(recipe.getTitle());
            }
            // Refresh required ingredients in ViewModel after batch mutation
            viewModel.refreshRequiredIngredients(requireContext());
            
            // Refresh the item
            adapter.notifyItemChanged(position);
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Just dismiss
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        // Close any open swipes when returning
        if (swipeHelper != null && recyclerView != null) {
            swipeHelper.closeOpenItem(recyclerView);
        }
    }

    // Sample data moved to RecipeViewModel
}