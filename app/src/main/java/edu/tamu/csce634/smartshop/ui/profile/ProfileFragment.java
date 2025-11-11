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

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.databinding.FragmentProfileBinding;
import edu.tamu.csce634.smartshop.models.ProfileData;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        // **IMPORTANT: Get the ViewModel scoped to the Activity**
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the dropdown adapter
        String[] goals = getResources().getStringArray(R.array.profile_goals);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, goals);
        binding.editGoal.setAdapter(adapter);

        // Observe the LiveData and populate the fields
        profileViewModel.getProfileData().observe(getViewLifecycleOwner(), profileData -> {
            if (profileData != null) {
                binding.editName.setText(profileData.getName());
                binding.editEmail.setText(profileData.getEmail());
                binding.editHeight.setText(String.valueOf(profileData.getHeight()));
                binding.editWeight.setText(String.valueOf(profileData.getWeight()));
                binding.editGoal.setText(profileData.getGoal(), false); // false to not filter
                binding.editStore.setText(profileData.getStore());
            }
        });

        // Handle Back button click
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        // Save button listener has been removed.
    }

    @Override
    public void onPause() {
        super.onPause();
        // Auto-save data when the user leaves the fragment
        autoSaveProfileData();
    }

    private void autoSaveProfileData() {
        try {
            // Check if binding is null (can happen if onPause is called after onDestroyView)
            if (binding == null) {
                return;
            }

            String name = binding.editName.getText().toString();
            String email = binding.editEmail.getText().toString();

            // Don't save if essential fields are empty
            if (name.isEmpty() || email.isEmpty()) {
                return;
            }

            int height = Integer.parseInt(binding.editHeight.getText().toString());
            int weight = Integer.parseInt(binding.editWeight.getText().toString());
            String goal = binding.editGoal.getText().toString();
            String store = binding.editStore.getText().toString();

            ProfileData data = new ProfileData(name, email, height, weight, goal, store);
            profileViewModel.saveProfile(data);

        } catch (NumberFormatException e) {
            // Silently fail if numbers are not valid (e.g., empty or "abc").
            // The user will see their invalid text when they return.
            Log.e("ProfileFragment", "Could not auto-save profile due to invalid number", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}