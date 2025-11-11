package edu.tamu.csce634.smartshop.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import edu.tamu.csce634.smartshop.Repository.ProfileRepository;
import edu.tamu.csce634.smartshop.models.ProfileData;


public class ProfileViewModel extends AndroidViewModel {

    private ProfileRepository repository;
    private LiveData<ProfileData> profileData;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new ProfileRepository(application);
        profileData = repository.getProfileData();
    }

    public LiveData<ProfileData> getProfileData() {
        return profileData;
    }

    public void saveProfile(ProfileData data) {
        repository.saveProfile(data);
    }
}