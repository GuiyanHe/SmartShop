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
            }
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

            ProfileData data = new ProfileData(name, email, height, weight, goal, store,
                    calorieGoal, proteinGoal, fatGoal, waterGoal);
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