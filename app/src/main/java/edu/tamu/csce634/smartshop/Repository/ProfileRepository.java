package edu.tamu.csce634.smartshop.Repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.tamu.csce634.smartshop.models.ProfileData;

public class ProfileRepository {
    private static final String PREF_NAME = "SmartShopProfilePrefs";
    // Basic Info
    private static final String KEY_NAME = "KEY_NAME";
    private static final String KEY_EMAIL = "KEY_EMAIL";
    private static final String KEY_HEIGHT = "KEY_HEIGHT";
    private static final String KEY_WEIGHT = "KEY_WEIGHT";
    private static final String KEY_GOAL = "KEY_GOAL";
    private static final String KEY_STORE = "KEY_STORE";
    // Goals
    private static final String KEY_CALORIE_GOAL = "KEY_CALORIE_GOAL";
    private static final String KEY_PROTEIN_GOAL = "KEY_PROTEIN_GOAL";
    private static final String KEY_FAT_GOAL = "KEY_FAT_GOAL";
    private static final String KEY_WATER_GOAL = "KEY_WATER_GOAL";

    // Shopping Preferences Keys
    private static final String KEY_IS_VEGETARIAN = "KEY_IS_VEGETARIAN";
    private static final String KEY_IS_VEGAN = "KEY_IS_VEGAN";
    private static final String KEY_ORGANIC_ONLY = "KEY_ORGANIC_ONLY";
    private static final String KEY_GLUTEN_FREE = "KEY_GLUTEN_FREE";
    private static final String KEY_ALLERGIES = "KEY_ALLERGIES";
    private static final String KEY_FAVORITE_BRANDS = "KEY_FAVORITE_BRANDS";
    private static final String KEY_MAX_ITEM_PRICE = "KEY_MAX_ITEM_PRICE";
    private static final String KEY_PREFER_DEALS = "KEY_PREFER_DEALS";
    private static final String KEY_PACKAGE_SIZE_PREF = "KEY_PACKAGE_SIZE_PREF";

    private SharedPreferences sharedPrefs;
    private MutableLiveData<ProfileData> profileDataLiveData = new MutableLiveData<>();

    public ProfileRepository(Application application) {
        sharedPrefs = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadProfile();
    }

    private void loadProfile() {
        ProfileData defaults = ProfileData.createDefault();
        // Basic Info
        String name = sharedPrefs.getString(KEY_NAME, defaults.getName());
        String email = sharedPrefs.getString(KEY_EMAIL, defaults.getEmail());
        int height = sharedPrefs.getInt(KEY_HEIGHT, defaults.getHeight());
        int weight = sharedPrefs.getInt(KEY_WEIGHT, defaults.getWeight());
        String goal = sharedPrefs.getString(KEY_GOAL, defaults.getGoal());
        String store = sharedPrefs.getString(KEY_STORE, defaults.getStore());
        // Goals
        int calorieGoal = sharedPrefs.getInt(KEY_CALORIE_GOAL, defaults.getCalorieGoal());
        int proteinGoal = sharedPrefs.getInt(KEY_PROTEIN_GOAL, defaults.getProteinGoal());
        int fatGoal = sharedPrefs.getInt(KEY_FAT_GOAL, defaults.getFatGoal());
        int waterGoal = sharedPrefs.getInt(KEY_WATER_GOAL, defaults.getWaterGoal());

        // Load Preferences
        boolean isVegetarian = sharedPrefs.getBoolean(KEY_IS_VEGETARIAN, defaults.isVegetarian());
        boolean isVegan = sharedPrefs.getBoolean(KEY_IS_VEGAN, defaults.isVegan());
        boolean organicOnly = sharedPrefs.getBoolean(KEY_ORGANIC_ONLY, defaults.isOrganicOnly());
        boolean glutenFree = sharedPrefs.getBoolean(KEY_GLUTEN_FREE, defaults.isGlutenFree());
        String allergies = sharedPrefs.getString(KEY_ALLERGIES, defaults.getAllergies());
        String favoriteBrands = sharedPrefs.getString(KEY_FAVORITE_BRANDS, defaults.getFavoriteBrands());
        float maxItemPrice = sharedPrefs.getFloat(KEY_MAX_ITEM_PRICE, defaults.getMaxItemPrice());
        if (maxItemPrice < 5.0f || maxItemPrice > 50.0f) {
            maxItemPrice = 20.0f; // safe default value
        }
        boolean preferDeals = sharedPrefs.getBoolean(KEY_PREFER_DEALS, defaults.isPreferDeals());
        String packageSizePref = sharedPrefs.getString(KEY_PACKAGE_SIZE_PREF, defaults.getPackageSizePref());

        profileDataLiveData.postValue(new ProfileData(
                name, email, height, weight, goal, store,
                calorieGoal, proteinGoal, fatGoal, waterGoal,
                isVegetarian, isVegan, organicOnly, glutenFree,
                allergies, favoriteBrands, maxItemPrice, preferDeals, packageSizePref
        ));
    }

    public LiveData<ProfileData> getProfileData() {
        return profileDataLiveData;
    }

    public void saveProfile(ProfileData data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        // Basic Info
        editor.putString(KEY_NAME, data.getName());
        editor.putString(KEY_EMAIL, data.getEmail());
        editor.putInt(KEY_HEIGHT, data.getHeight());
        editor.putInt(KEY_WEIGHT, data.getWeight());
        editor.putString(KEY_GOAL, data.getGoal());
        editor.putString(KEY_STORE, data.getStore());
        // Goals
        editor.putInt(KEY_CALORIE_GOAL, data.getCalorieGoal());
        editor.putInt(KEY_PROTEIN_GOAL, data.getProteinGoal());
        editor.putInt(KEY_FAT_GOAL, data.getFatGoal());
        editor.putInt(KEY_WATER_GOAL, data.getWaterGoal());
        // Save Preferences
        editor.putBoolean(KEY_IS_VEGETARIAN, data.isVegetarian());
        editor.putBoolean(KEY_IS_VEGAN, data.isVegan());
        editor.putBoolean(KEY_ORGANIC_ONLY, data.isOrganicOnly());
        editor.putBoolean(KEY_GLUTEN_FREE, data.isGlutenFree());
        editor.putString(KEY_ALLERGIES, data.getAllergies());
        editor.putString(KEY_FAVORITE_BRANDS, data.getFavoriteBrands());
        editor.putFloat(KEY_MAX_ITEM_PRICE, data.getMaxItemPrice());
        editor.putBoolean(KEY_PREFER_DEALS, data.isPreferDeals());
        editor.putString(KEY_PACKAGE_SIZE_PREF, data.getPackageSizePref());

        editor.apply();
        profileDataLiveData.postValue(data);
    }
}