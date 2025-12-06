package edu.tamu.csce634.smartshop.Repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.tamu.csce634.smartshop.models.DailyStats;

public class DailyStatsRepository {
    private static final String PREF_NAME = "SmartShopDailyStatsPrefs";
    private static final String KEY_CURRENT_CALORIES = "KEY_CURRENT_CALORIES";
    private static final String KEY_CURRENT_PROTEIN = "KEY_CURRENT_PROTEIN";
    private static final String KEY_CURRENT_FAT = "KEY_CURRENT_FAT";
    private static final String KEY_CURRENT_WATER = "KEY_CURRENT_WATER";

    private SharedPreferences sharedPrefs;
    private MutableLiveData<DailyStats> dailyStatsLiveData = new MutableLiveData<>();

    public DailyStatsRepository(Application application) {
        sharedPrefs = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadStats();
    }

    private void loadStats() {
        int calories = sharedPrefs.getInt(KEY_CURRENT_CALORIES, 0);
        int protein = sharedPrefs.getInt(KEY_CURRENT_PROTEIN, 0);
        int fat = sharedPrefs.getInt(KEY_CURRENT_FAT, 0);
        int water = sharedPrefs.getInt(KEY_CURRENT_WATER, 0);

        // CHANGE: postValue -> setValue (immediate update)
        dailyStatsLiveData.setValue(new DailyStats(calories, protein, fat, water));
    }

    public LiveData<DailyStats> getDailyStats() {
        return dailyStatsLiveData;
    }

    private DailyStats getCurrentStats() {
        return dailyStatsLiveData.getValue() != null
                ? dailyStatsLiveData.getValue()
                : DailyStats.createDefault();
    }

    public void addCalories(int amount) {
        DailyStats current = getCurrentStats();
        int newCalories = current.getCurrentCalories() + amount;

        sharedPrefs.edit().putInt(KEY_CURRENT_CALORIES, newCalories).apply();

        // CHANGE
        dailyStatsLiveData.setValue(
                new DailyStats(
                        newCalories,
                        current.getCurrentProtein(),
                        current.getCurrentFat(),
                        current.getCurrentWater()
                )
        );
    }

    public void addProtein(int amount) {
        DailyStats current = getCurrentStats();
        int newProtein = current.getCurrentProtein() + amount;

        sharedPrefs.edit().putInt(KEY_CURRENT_PROTEIN, newProtein).apply();

        // CHANGE
        dailyStatsLiveData.setValue(
                new DailyStats(
                        current.getCurrentCalories(),
                        newProtein,
                        current.getCurrentFat(),
                        current.getCurrentWater()
                )
        );
    }

    public void addFat(int amount) {
        DailyStats current = getCurrentStats();
        int newFat = current.getCurrentFat() + amount;

        sharedPrefs.edit().putInt(KEY_CURRENT_FAT, newFat).apply();

        // CHANGE
        dailyStatsLiveData.setValue(
                new DailyStats(
                        current.getCurrentCalories(),
                        current.getCurrentProtein(),
                        newFat,
                        current.getCurrentWater()
                )
        );
    }

    public void addWater(int amount) {
        DailyStats current = getCurrentStats();
        int newWater = current.getCurrentWater() + amount;

        sharedPrefs.edit().putInt(KEY_CURRENT_WATER, newWater).apply();

        // CHANGE
        dailyStatsLiveData.setValue(
                new DailyStats(
                        current.getCurrentCalories(),
                        current.getCurrentProtein(),
                        current.getCurrentFat(),
                        newWater
                )
        );
    }

    public void resetStats() {
        sharedPrefs.edit().clear().apply();

        // CHANGE
        dailyStatsLiveData.setValue(DailyStats.createDefault());
    }
}
