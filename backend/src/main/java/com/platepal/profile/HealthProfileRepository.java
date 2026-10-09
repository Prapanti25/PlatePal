package com.platepal.profile;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    Optional<HealthProfile> findByUser_FirebaseUid(String firebaseUid);
}