package edu.tamu.csce634.smartshop.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import edu.tamu.csce634.smartshop.Repository.DailyStatsRepository;
import edu.tamu.csce634.smartshop.models.DailyStats;


public class DailyStatsViewModel extends AndroidViewModel {

    private DailyStatsRepository repository;
    private LiveData<DailyStats> dailyStats;

    public DailyStatsViewModel(@NonNull Application application) {
        super(application);
        repository = new DailyStatsRepository(application);
        dailyStats = repository.getDailyStats();
    }

    public LiveData<DailyStats> getDailyStats() {
        return dailyStats;
    }

    public void addCalories(int amount) {
        repository.addCalories(amount);
    }

    public void addProtein(int amount) {
        repository.addProtein(amount);
    }

    public void addFat(int amount) {
        repository.addFat(amount);
    }

    public void addWater(int amount) {
        repository.addWater(amount);
    }
}