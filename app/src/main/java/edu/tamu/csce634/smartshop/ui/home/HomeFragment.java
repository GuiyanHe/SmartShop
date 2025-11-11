package edu.tamu.csce634.smartshop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Locale;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.databinding.FragmentHomeBinding;
import edu.tamu.csce634.smartshop.ui.profile.ProfileViewModel; // <-- Import shared ViewModel

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ProfileViewModel profileViewModel; // <-- Add shared ViewModel

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Note: The original HomeViewModel is not used here, but you can keep it
        // for other home-screen-specific logic if you want.
        // HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // **IMPORTANT: Get the ViewModel scoped to the Activity**
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.imageProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_profile)
        );
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe the shared profile data
        profileViewModel.getProfileData().observe(getViewLifecycleOwner(), profileData -> {
            if (profileData != null) {
                binding.textGreeting.setText("Hello, " + profileData.getName() + "!");
                binding.textHeightValue.setText(String.format(Locale.getDefault(), "%d cm", profileData.getHeight()));
                binding.textWeightValue.setText(String.format(Locale.getDefault(), "%d kg", profileData.getWeight()));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}