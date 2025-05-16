package com.gdc.tripmate.domain.tag.repository;

import com.gdc.tripmate.domain.tag.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
    Optional<UserProfile> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
}