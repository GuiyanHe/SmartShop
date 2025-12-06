package edu.tamu.csce634.smartshop.ui.feedback;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.managers.RecipeManager;
import edu.tamu.csce634.smartshop.models.Recipe;
import edu.tamu.csce634.smartshop.ui.home.DailyStatsViewModel;

public class FeedbackFragment extends Fragment {

    private ImageView mealPhoto;
    private Uri pickedImageUri = null;

    private AutoCompleteTextView dropdownMeal;
    private TextInputEditText servingsInput, notesInput;
    private RatingBar ratingBar;

    private List<Recipe> recipeList;
    private Recipe selectedRecipe = null;

    private DailyStatsViewModel dailyStatsViewModel;   // ⭐ FIX: Shared ViewModel

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    pickedImageUri = uri;
                    if (mealPhoto != null) mealPhoto.setImageURI(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Back arrow
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        // ⭐ FIX: Use requireActivity() so HomeFragment and FeedbackFragment SHARE LiveData
        dailyStatsViewModel = new ViewModelProvider(requireActivity())
                .get(DailyStatsViewModel.class);

        mealPhoto = view.findViewById(R.id.image_meal);
        Button btnPick = view.findViewById(R.id.btn_pick_photo);
        Button btnDemo = view.findViewById(R.id.btn_demo_photo);

        dropdownMeal = view.findViewById(R.id.dropdown_meal);
        servingsInput = view.findViewById(R.id.input_servings);
        notesInput = view.findViewById(R.id.input_notes);
        ratingBar = view.findViewById(R.id.rating_bar);

        // -------------------------------
        // LOAD RECIPES INTO DROPDOWN
        // -------------------------------
        RecipeManager rm = RecipeManager.getInstance(requireContext());
        recipeList = rm.getAllRecipes();

        List<String> names = new ArrayList<>();
        for (Recipe r : recipeList) names.add(r.getTitle());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                names
        );
        dropdownMeal.setAdapter(adapter);

        dropdownMeal.setOnItemClickListener((parent, v, pos, id) -> {
            selectedRecipe = recipeList.get(pos);

            // Auto update meal image
            pickedImageUri = null;
            mealPhoto.setImageResource(selectedRecipe.getImageResId());
        });

        // -------------------------------
        // PHOTO BUTTONS
        // -------------------------------
        btnPick.setOnClickListener(v -> pickImage.launch("image/*"));
        btnDemo.setOnClickListener(v -> {
            pickedImageUri = null;

            if (selectedRecipe != null) {
                mealPhoto.setImageResource(selectedRecipe.getImageResId());
            } else {
                mealPhoto.setImageResource(R.drawable.avocado_salad);
            }
        });

        // -------------------------------
        // SUBMIT BUTTON
        // -------------------------------
        view.findViewById(R.id.btn_submit).setOnClickListener(this::handleSubmit);
    }


    // -----------------------------------------------------------------------------------
    // SUBMIT HANDLER
    // -----------------------------------------------------------------------------------
    private void handleSubmit(View v) {

        if (selectedRecipe == null) {
            showDialog("Missing Meal", "Please select a meal before submitting.");
            return;
        }

        String servingsStr = (servingsInput.getText() == null) ? "" : servingsInput.getText().toString().trim();
        if (servingsStr.isEmpty()) {
            showDialog("Missing Servings", "Please enter the number of servings.");
            return;
        }

        int servings;
        try {
            servings = Integer.parseInt(servingsStr);
            if (servings <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showDialog("Invalid Servings", "Enter a positive whole number.");
            return;
        }

        String notes = (notesInput.getText() == null) ? "" : notesInput.getText().toString().trim();
        int rating = (int) ratingBar.getRating();

        // -------------------------------------------------------
        // APPLY NUTRITION GAINS (Shared ViewModel)
        // -------------------------------------------------------
        int calories = selectedRecipe.getCalories() * servings;
        int protein  = selectedRecipe.getProtein() * servings;
        int fat      = selectedRecipe.getFat() * servings;
        int water    = selectedRecipe.getWater() * servings;

        // ⭐ FIX: Update shared LiveData. HomeFragment observes this and updates instantly.
        dailyStatsViewModel.addCalories(calories);
        dailyStatsViewModel.addProtein(protein);
        dailyStatsViewModel.addFat(fat);
        dailyStatsViewModel.addWater(water);

        // -------------------------------------------------------
        // BUILD FEEDBACK DIALOG MESSAGE
        // -------------------------------------------------------
        String message =
                "Meal Logged Successfully!\n\n" +
                        "Meal: " + selectedRecipe.getTitle() + "\n" +
                        "Servings: " + servings + "\n\n" +
                        "Nutrition Added:\n" +
                        "  • +" + calories + " Calories\n" +
                        "  • +" + protein + "g Protein\n" +
                        "  • +" + fat + "g Fat\n" +
                        "  • +" + water + " cups Water\n\n" +
                        "Pantry updated based on ingredient usage.\n" +
                        "Recommendations will adjust automatically.\n\n" +
                        (notes.isEmpty() ? "" : ("Notes:\n" + notes));

        new AlertDialog.Builder(requireContext())
                .setTitle("Feedback Submitted!")
                .setMessage(message)
                .setPositiveButton("Done", (d, w) ->
                        Navigation.findNavController(v).popBackStack()
                )
                .show();
    }

    private void showDialog(String title, String msg) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
}