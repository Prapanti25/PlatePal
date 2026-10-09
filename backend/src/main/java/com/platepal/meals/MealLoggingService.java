package com.platepal.meals;

import java.util.List;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.platepal.profile.AppUser;
import com.platepal.profile.AppUserRepository;

@Service
public class MealLoggingService {
    private final AppUserRepository userRepository;
    private final LoggedMealRepository mealRepository;
    private final VisionAnalysisService visionAnalysisService;

    public MealLoggingService(
            AppUserRepository userRepository,
            LoggedMealRepository mealRepository,
            VisionAnalysisService visionAnalysisService) {
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
        this.visionAnalysisService = visionAnalysisService;
    }

    @Transactional
    public LoggedMealResponse log(String firebaseUid, String imageUrl) {
        AppUser user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));
        VisionAnalysis analysis = visionAnalysisService.analyze(imageUrl);
        return LoggedMealResponse.from(mealRepository.save(new LoggedMeal(user, imageUrl, analysis)));
    }

    @Transactional(readOnly = true)
    public List<LoggedMealResponse> recent(String firebaseUid) {
        return mealRepository.findTop20ByUser_FirebaseUidOrderByLoggedAtDesc(firebaseUid).stream()
                .map(LoggedMealResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoggedMealResponse> today(String firebaseUid) {
        var startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        return mealRepository.findAllByUser_FirebaseUidAndLoggedAtGreaterThanEqualOrderByLoggedAtDesc(
                        firebaseUid, startOfDay).stream()
                .map(LoggedMealResponse::from)
                .toList();
    }
}