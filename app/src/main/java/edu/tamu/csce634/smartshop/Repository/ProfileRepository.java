package edu.tamu.csce634.smartshop.Repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.tamu.csce634.smartshop.models.ProfileData;

public class ProfileRepository {
    private static final String PREF_NAME = "SmartShopProfilePrefs";
    private static final String KEY_NAME = "KEY_NAME";
    private static final String KEY_EMAIL = "KEY_EMAIL";
    private static final String KEY_HEIGHT = "KEY_HEIGHT";
    private static final String KEY_WEIGHT = "KEY_WEIGHT";
    private static final String KEY_GOAL = "KEY_GOAL";
    private static final String KEY_STORE = "KEY_STORE";

    private SharedPreferences sharedPrefs;
    private MutableLiveData<ProfileData> profileDataLiveData = new MutableLiveData<>();

    public ProfileRepository(Application application) {
        sharedPrefs = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadProfile();
    }

    // Load data from SharedPreferences and post it to LiveData
    private void loadProfile() {
        ProfileData defaults = ProfileData.createDefault();
        String name = sharedPrefs.getString(KEY_NAME, defaults.getName());
        String email = sharedPrefs.getString(KEY_EMAIL, defaults.getEmail());
        int height = sharedPrefs.getInt(KEY_HEIGHT, defaults.getHeight());
        int weight = sharedPrefs.getInt(KEY_WEIGHT, defaults.getWeight());
        String goal = sharedPrefs.getString(KEY_GOAL, defaults.getGoal());
        String store = sharedPrefs.getString(KEY_STORE, defaults.getStore());

        profileDataLiveData.postValue(new ProfileData(name, email, height, weight, goal, store));
    }

    // Public getter for the LiveData
    public LiveData<ProfileData> getProfileData() {
        return profileDataLiveData;
    }

    // Save data to SharedPreferences and update LiveData
    public void saveProfile(ProfileData data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_NAME, data.getName());
        editor.putString(KEY_EMAIL, data.getEmail());
        editor.putInt(KEY_HEIGHT, data.getHeight());
        editor.putInt(KEY_WEIGHT, data.getWeight());
        editor.putString(KEY_GOAL, data.getGoal());
        editor.putString(KEY_STORE, data.getStore());
        editor.apply();

        // Update the LiveData to notify observers
        profileDataLiveData.postValue(data);
    }
}
