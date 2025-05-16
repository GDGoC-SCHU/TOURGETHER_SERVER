package com.gdc.tripmate.domain.tag.repository;

import com.gdc.tripmate.domain.tag.entity.UserProfileTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileTagRepository extends JpaRepository<UserProfileTag, Long> {
    List<UserProfileTag> findAllByUserProfileId(Long profileId);
    
    @Modifying
    @Query("DELETE FROM UserProfileTag upt WHERE upt.userProfile.id = :profileId")
    void deleteAllByUserProfileId(Long profileId);
}