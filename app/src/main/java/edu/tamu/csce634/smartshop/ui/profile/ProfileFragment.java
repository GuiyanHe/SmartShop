package edu.tamu.csce634.smartshop.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.databinding.FragmentProfileBinding;
import edu.tamu.csce634.smartshop.models.ProfileData;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] goals = getResources().getStringArray(R.array.profile_goals);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, goals);
        binding.editGoal.setAdapter(adapter);

        profileViewModel.getProfileData().observe(getViewLifecycleOwner(), profileData -> {
            if (profileData != null) {
                // Basic Info
                binding.editName.setText(profileData.getName());
                binding.editEmail.setText(profileData.getEmail());
                binding.editHeight.setText(String.valueOf(profileData.getHeight()));
                binding.editWeight.setText(String.valueOf(profileData.getWeight()));
                binding.editGoal.setText(profileData.getGoal(), false);
                binding.editStore.setText(profileData.getStore());

                // Goals
                binding.editCalorieGoal.setText(String.valueOf(profileData.getCalorieGoal()));
                binding.editProteinGoal.setText(String.valueOf(profileData.getProteinGoal()));
                binding.editFatGoal.setText(String.valueOf(profileData.getFatGoal()));
                binding.editWaterGoal.setText(String.valueOf(profileData.getWaterGoal()));

                // Dietary Preferences
                binding.switchVegetarian.setChecked(profileData.isVegetarian());
                binding.switchVegan.setChecked(profileData.isVegan());
                binding.switchOrganicOnly.setChecked(profileData.isOrganicOnly());
                binding.switchGlutenFree.setChecked(profileData.isGlutenFree());

                // Allergies
                List<String> allergies = profileData.getAllergiesList();
                binding.chipAllergyNuts.setChecked(allergies.contains("nuts"));
                binding.chipAllergyShellfish.setChecked(allergies.contains("shellfish"));
                binding.chipAllergyDairy.setChecked(allergies.contains("dairy"));
                binding.chipAllergySoy.setChecked(allergies.contains("soy"));
                binding.chipAllergyEggs.setChecked(allergies.contains("eggs"));

                // Favorite Brands
                List<String> brands = profileData.getFavoriteBrandsList();
                binding.chipBrandGreenvalley.setChecked(brands.contains("GreenValley"));
                binding.chipBrandStorebrand.setChecked(brands.contains("StoreBrand"));
                binding.chipBrandNaturebest.setChecked(brands.contains("NatureBest"));
                binding.chipBrandSunharvest.setChecked(brands.contains("SunHarvest"));

                // Budget
                float maxPrice = profileData.getMaxItemPrice();
                // check for invalid values
                if (maxPrice < 5.0f) maxPrice = 5.0f;
                if (maxPrice > 50.0f) maxPrice = 50.0f;

                binding.sliderMaxPrice.setValue(maxPrice);
                binding.textMaxPrice.setText(String.format(Locale.US, "$%.2f", maxPrice));binding.switchPreferDeals.setChecked(profileData.isPreferDeals());

                // Package Size Preference
                String packagePref = profileData.getPackageSizePref();
                if ("small".equals(packagePref)) {
                    binding.radioPackageSmall.setChecked(true);
                } else if ("large".equals(packagePref)) {
                    binding.radioPackageLarge.setChecked(true);
                } else {
                    binding.radioPackageRegular.setChecked(true);
                }
            }
        });

        binding.sliderMaxPrice.addOnChangeListener((slider, value, fromUser) -> {
            binding.textMaxPrice.setText(String.format(Locale.US, "$%.2f", value));
        });

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        autoSaveProfileData();
    }

    private void autoSaveProfileData() {
        try {
            if (binding == null) return;

            // Basic Info
            String name = binding.editName.getText().toString();
            String email = binding.editEmail.getText().toString();
            if (name.isEmpty() || email.isEmpty()) return;
            int height = Integer.parseInt(binding.editHeight.getText().toString());
            int weight = Integer.parseInt(binding.editWeight.getText().toString());
            String goal = binding.editGoal.getText().toString();
            String store = binding.editStore.getText().toString();

            // Goals
            int calorieGoal = Integer.parseInt(binding.editCalorieGoal.getText().toString());
            int proteinGoal = Integer.parseInt(binding.editProteinGoal.getText().toString());
            int fatGoal = Integer.parseInt(binding.editFatGoal.getText().toString());
            int waterGoal = Integer.parseInt(binding.editWaterGoal.getText().toString());

            // Dietary Preferences
            boolean isVegetarian = binding.switchVegetarian.isChecked();
            boolean isVegan = binding.switchVegan.isChecked();
            boolean organicOnly = binding.switchOrganicOnly.isChecked();
            boolean glutenFree = binding.switchGlutenFree.isChecked();

            // Allergies
            List<String> allergiesList = new ArrayList<>();
            if (binding.chipAllergyNuts.isChecked()) allergiesList.add("nuts");
            if (binding.chipAllergyShellfish.isChecked()) allergiesList.add("shellfish");
            if (binding.chipAllergyDairy.isChecked()) allergiesList.add("dairy");
            if (binding.chipAllergySoy.isChecked()) allergiesList.add("soy");
            if (binding.chipAllergyEggs.isChecked()) allergiesList.add("eggs");
            String allergies = new JSONArray(allergiesList).toString();

            // Favorite Brands
            List<String> brandsList = new ArrayList<>();
            if (binding.chipBrandGreenvalley.isChecked()) brandsList.add("GreenValley");
            if (binding.chipBrandStorebrand.isChecked()) brandsList.add("StoreBrand");
            if (binding.chipBrandNaturebest.isChecked()) brandsList.add("NatureBest");
            if (binding.chipBrandSunharvest.isChecked()) brandsList.add("SunHarvest");
            String favoriteBrands = new JSONArray(brandsList).toString();

            // Budget
            float maxItemPrice = binding.sliderMaxPrice.getValue();
            boolean preferDeals = binding.switchPreferDeals.isChecked();

            // Package Size
            String packageSizePref = "regular";
            if (binding.radioPackageSmall.isChecked()) {
                packageSizePref = "small";
            } else if (binding.radioPackageLarge.isChecked()) {
                packageSizePref = "large";
            }

            ProfileData data = new ProfileData(
                    name, email, height, weight, goal, store,
                    calorieGoal, proteinGoal, fatGoal, waterGoal,
                    isVegetarian, isVegan, organicOnly, glutenFree,
                    allergies, favoriteBrands, maxItemPrice, preferDeals, packageSizePref
            );

            profileViewModel.saveProfile(data);

        } catch (NumberFormatException e) {
            Log.e("ProfileFragment", "Could not auto-save profile due to invalid number", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}