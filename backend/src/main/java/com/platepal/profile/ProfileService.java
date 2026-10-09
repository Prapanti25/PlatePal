package com.platepal.profile;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final AppUserRepository userRepository;
    private final HealthProfileRepository profileRepository;

    public ProfileService(AppUserRepository userRepository, HealthProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public ProfileResponse save(String firebaseUid, ProfileRequest request) {
        AppUser user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> userRepository.save(new AppUser(firebaseUid)));
        HealthProfile profile = profileRepository.findByUser_FirebaseUid(firebaseUid)
                .orElseGet(() -> new HealthProfile(user));
        profile.setCalorieTarget(request.calorieTarget());
        profile.setHouseholdSize(request.householdSize());
        profile.setWeeklyBudgetBdt(request.weeklyBudgetBdt());
        profile.setDietaryTags(request.dietaryTags().stream().map(String::trim).distinct().toList());
        return ProfileResponse.from(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public Optional<ProfileResponse> find(String firebaseUid) {
        return profileRepository.findByUser_FirebaseUid(firebaseUid).map(ProfileResponse::from);
    }
}