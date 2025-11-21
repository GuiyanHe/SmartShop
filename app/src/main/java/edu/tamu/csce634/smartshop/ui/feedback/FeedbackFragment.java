package edu.tamu.csce634.smartshop.ui.feedback;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;

import edu.tamu.csce634.smartshop.R;

public class FeedbackFragment extends Fragment {

    private ImageView mealPhoto;
    private Uri pickedImageUri = null;

    private TextInputEditText mealNameInput, notesInput;
    private RadioGroup portionGroup;
    private RatingBar ratingBar;

    private TextView item1Mark, item2Mark, item3Mark, item1Status, item2Status, item3Status;
    private boolean item1Purchased = false, item2Purchased = false, item3Purchased = false;

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

        mealPhoto = view.findViewById(R.id.image_meal);
        Button btnPick = view.findViewById(R.id.btn_pick_photo);
        Button btnDemo = view.findViewById(R.id.btn_demo_photo);

        mealNameInput = view.findViewById(R.id.input_meal_name);
        notesInput = view.findViewById(R.id.input_notes);
        portionGroup = view.findViewById(R.id.portion_group);
        ratingBar = view.findViewById(R.id.rating_bar);

        // Item taps toggle “purchased”
        item1Mark = view.findViewById(R.id.item1_mark);
        item2Mark = view.findViewById(R.id.item2_mark);
        item3Mark = view.findViewById(R.id.item3_mark);

        item1Status = new TextView(requireContext());
        item2Status = new TextView(requireContext());
        item3Status = new TextView(requireContext());

        item1Mark.setOnClickListener(v -> togglePurchased(1));
        item2Mark.setOnClickListener(v -> togglePurchased(2));
        item3Mark.setOnClickListener(v -> togglePurchased(3));

        ((TextView) view.findViewById(R.id.mark_all)).setOnClickListener(v -> {
            item1Purchased = item2Purchased = item3Purchased = true;
            refreshPills();
        });

        // Photo buttons
        btnPick.setOnClickListener(v -> pickImage.launch("image/*"));
        btnDemo.setOnClickListener(v -> {
            pickedImageUri = null;
            mealPhoto.setImageResource(R.drawable.avocado_salad);
        });

        // Submit (show a fake “what would happen” summary)
        view.findViewById(R.id.btn_submit).setOnClickListener(v -> {
            String meal = textOrBlank(mealNameInput);
            String portion = getPortion();
            String notes = textOrBlank(notesInput);
            String purchasedSummary =
                    (item1Purchased ? "Chicken, " : "") +
                            (item2Purchased ? "Brown Rice, " : "") +
                            (item3Purchased ? "Broccoli, " : "");
            if (purchasedSummary.endsWith(", "))
                purchasedSummary = purchasedSummary.substring(0, purchasedSummary.length() - 2);

            String message =
                    "SmartShop would now:\n" +
                            "• Update pantry to subtract used items\n" +
                            "• Adjust future recipe recommendations\n" +
                            "• Log nutrition estimate from photo (AI step)\n\n" +
                            "Meal: " + (meal.isEmpty() ? "(unnamed)" : meal) + "\n" +
                            "Portion: " + portion + "\n" +
                            "Items used: " + (purchasedSummary.isEmpty() ? "(none marked)" : purchasedSummary) + "\n" +
                            "Rating: " + (int) ratingBar.getRating() + "/5\n" +
                            (notes.isEmpty() ? "" : ("Notes: " + notes));

            new AlertDialog.Builder(requireContext())
                    .setTitle("Feedback submitted!")
                    .setMessage(message)
                    .setPositiveButton("Done", (d, w) -> Navigation.findNavController(v).popBackStack())
                    .show();
        });
    }

    private void togglePurchased(int which) {
        if (which == 1) item1Purchased = !item1Purchased;
        if (which == 2) item2Purchased = !item2Purchased;
        if (which == 3) item3Purchased = !item3Purchased;
        refreshPills();
    }

    private void refreshPills() {
        item1Mark.setText(item1Purchased ? "Chicken Breast (Purchased ✓) — tap to undo" : "Chicken Breast (Tap to Mark Purchased)");
        item2Mark.setText(item2Purchased ? "Brown Rice (Purchased ✓) — tap to undo" : "Brown Rice (Tap to Mark Purchased)");
        item3Mark.setText(item3Purchased ? "Broccoli Crown (Purchased ✓) — tap to undo" : "Broccoli Crown (Tap to Mark Purchased)");
    }

    private String getPortion() {
        int id = portionGroup.getCheckedRadioButtonId();
        if (id == R.id.portion_small) return "Small";
        if (id == R.id.portion_large) return "Large";
        return "Regular";
    }

    private String textOrBlank(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }
}
