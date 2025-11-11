package edu.tamu.csce634.smartshop.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Locale;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.databinding.FragmentHomeBinding;
import edu.tamu.csce634.smartshop.models.DailyStats;
import edu.tamu.csce634.smartshop.models.ProfileData;
import edu.tamu.csce634.smartshop.ui.profile.ProfileViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ProfileViewModel profileViewModel;
    private DailyStatsViewModel dailyStatsViewModel;

    // Cache for quick UI updates
    private ProfileData mProfileData;
    private DailyStats mDailyStats;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Init ViewModels
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        dailyStatsViewModel = new ViewModelProvider(this).get(DailyStatsViewModel.class);

        binding.imageProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_home_to_profile)
        );

        // Set listeners for test buttons
        binding.buttonAddCalories.setOnClickListener(v -> dailyStatsViewModel.addCalories(100));
        binding.buttonAddProtein.setOnClickListener(v -> dailyStatsViewModel.addProtein(10));
        binding.buttonAddFat.setOnClickListener(v -> dailyStatsViewModel.addFat(5));
        binding.buttonAddWater.setOnClickListener(v -> dailyStatsViewModel.addWater(1));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe Profile Data (Goals)
        profileViewModel.getProfileData().observe(getViewLifecycleOwner(), profileData -> {
            if (profileData != null) {
                mProfileData = profileData;
                // Update greeting and cards
                binding.textGreeting.setText("Hello, " + profileData.getName() + "!");
                binding.textHeightValue.setText(String.format(Locale.getDefault(), "%d cm", profileData.getHeight()));
                binding.textWeightValue.setText(String.format(Locale.getDefault(), "%d kg", profileData.getWeight()));

                updateStatsUI();
            }
        });

        // Observe Daily Stats (Current Values)
        dailyStatsViewModel.getDailyStats().observe(getViewLifecycleOwner(), dailyStats -> {
            if (dailyStats != null) {
                mDailyStats = dailyStats;
                updateStatsUI();
            }
        });
    }

    private void updateStatsUI() {
        // Wait until both data sources are loaded
        if (mProfileData == null || mDailyStats == null || getContext() == null) {
            return;
        }

        // === 1. Protein Stat (Green) ===
        binding.statProtein.textLabel.setText("Protein");
        binding.statProtein.progressView.setProgressColor(ContextCompat.getColor(getContext(), R.color.stat_protein_green));
        int proGoal = mProfileData.getProteinGoal();
        int proCurrent = mDailyStats.getCurrentProtein();
        int proProgress = (proGoal > 0) ? (int) (((float) proCurrent / proGoal) * 100) : 0;

        binding.statProtein.progressView.setProgress(proProgress);
        binding.statProtein.progressView.setProgressText(String.format(Locale.getDefault(), "%dg", proCurrent));
        binding.statProtein.progressView.setProgressTextColor(Color.BLACK); // As per design

        // === 2. Fat Stat (Orange) ===
        binding.statFat.textLabel.setText("Fat");
        binding.statFat.progressView.setProgressColor(ContextCompat.getColor(getContext(), R.color.stat_fat_orange));
        int fatGoal = mProfileData.getFatGoal();
        int fatCurrent = mDailyStats.getCurrentFat();
        int fatProgress = (fatGoal > 0) ? (int) (((float) fatCurrent / fatGoal) * 100) : 0;

        binding.statFat.progressView.setProgress(fatProgress);
        binding.statFat.progressView.setProgressText(String.format(Locale.getDefault(), "%dg", fatCurrent));
        binding.statFat.progressView.setProgressTextColor(Color.BLACK);

        // === 3. Calories Stat (Red) ===
        binding.statCalories.textLabel.setText("Calories");
        binding.statCalories.progressView.setProgressColor(ContextCompat.getColor(getContext(), R.color.stat_calories_red));
        int calGoal = mProfileData.getCalorieGoal();
        int calCurrent = mDailyStats.getCurrentCalories();
        int calProgress = (calGoal > 0) ? (int) (((float) calCurrent / calGoal) * 100) : 0;

        binding.statCalories.progressView.setProgress(calProgress);
        binding.statCalories.progressView.setProgressText(String.format(Locale.getDefault(), "%d", calCurrent));
        binding.statCalories.progressView.setProgressTextColor(Color.BLACK);

        // === 4. Water Stat (Blue) ===
        binding.statWater.textLabel.setText("Water");
        binding.statWater.progressView.setProgressColor(ContextCompat.getColor(getContext(), R.color.stat_water_blue));
        int waterGoal = mProfileData.getWaterGoal();
        int waterCurrent = mDailyStats.getCurrentWater();
        int waterProgress = (waterGoal > 0) ? (int) (((float) waterCurrent / waterGoal) * 100) : 0;

        binding.statWater.progressView.setProgress(waterProgress);
        binding.statWater.progressView.setProgressText(String.format(Locale.getDefault(), "%d / %d", waterCurrent, waterGoal));
        binding.statWater.progressView.setProgressTextColor(Color.BLACK);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}