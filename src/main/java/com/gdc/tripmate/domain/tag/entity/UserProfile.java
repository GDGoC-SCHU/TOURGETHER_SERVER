package com.gdc.tripmate.domain.tag.entity;

import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.global.TimeStamp;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * 사용자 프로필(UserProfile) 엔티티 - 프로필 정보 중심
 */
@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class UserProfile extends TimeStamp {

    /**
     * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 설정 닉네임(이름)
    @Column(nullable = true, unique = true)
    private String nickname;

    // 자기소개
    @Column(length = 500)
    private String bio;

    // 성별 (male, female, other)
    @Column
    private String gender;

    // 생년월일
    @Column
    private LocalDate birthDate;

    // 프로필 설정 완료 여부
    @Column
    private boolean profileCompleted = false;

    /**
     * 연관관계
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProfileTag> profileTags = new HashSet<>();

    /**
     * 생성자
     */
    @Builder
    public UserProfile(String nickname, String bio, String gender, LocalDate birthDate, User user) {
        this.nickname = nickname;
        this.bio = bio;
        this.gender = gender;
        this.birthDate = birthDate;
        this.profileCompleted = false;
        setUser(user);
    }

    /**
     * 서비스 메소드
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    /**
     * 연관관계 편의 메소드
     */
    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getProfile() != this) {
            user.setProfile(this);
        }
    }

    public void addTag(Tag tag) {
        UserProfileTag profileTag = new UserProfileTag(this, tag);
        this.profileTags.add(profileTag);
    }

    public void removeTags() {
        this.profileTags.clear();
    }
}