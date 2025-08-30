package com.studymate.domain.user.entity;

import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;
import com.studymate.domain.onboarding.domain.type.PartnerGenderType;
import com.studymate.domain.onboarding.entity.Language;
import com.studymate.domain.onboarding.domain.type.LearningExpectionType;
import com.studymate.domain.user.domain.type.UserGenderType;
import com.studymate.domain.user.domain.type.UserIdentityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "USER_ID")
    private UUID userId;

    @Column(name = "USER_IDENTITY")
    private String userIdentity;

    @Column(name = "USER_CREATED_AT")
    private LocalDateTime userCreatedAt;

    @Column(name = "BIRTHDAY")
    private String birthday;

    @Column(name = "GENDER")
    @Enumerated(EnumType.STRING)
    private UserGenderType userGenderType;

    @Column(name = "BIRTHYEAR")
    private String birthyear;

    @Column(name = "ENGLISH_NAME")
    private String englishName;

    @Column(name = "PROFILE_IMAGE")
    private String profileImage;

    @Column(name = "SELF_BIO", length = 1000)
    private String selfBio;

    @Column(name = "USER_DISABLE",nullable = false)
    private boolean userDisable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "LOCATION_ID")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn ( name = "NATIVE_LANG_ID")
    private Language nativeLanguage;

    @Column( name = "NAME")
    private String name;

    @Column(name = "LEARNING_EXPECTION")
    @Enumerated(EnumType.STRING)
    private LearningExpectionType learningExpectionType;

    @Column(name = "PARTNER_GENDER")
    @Enumerated(EnumType.STRING)
    private PartnerGenderType partnerGender;

    @Column(name = "COMMUNICATION_METHOD")
    @Enumerated(EnumType.STRING)
    private CommunicationMethodType communicationMethodType;

    @Column(name = "DAILY_MINUTE")
    @Enumerated(EnumType.STRING)
    private DailyMinuteType dailyMinuteType;

    @Column(name = "USER_IDENTITY_TYPE")
    @Enumerated(EnumType.STRING)
    private UserIdentityType userIdentityType;

    @Column(name = "IS_ONBOARDING_COMPLETED", nullable = false)
    @Builder.Default
    private Boolean isOnboardingCompleted = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserStatus userStatus;






    public void updateNaverProfile(String name, String birthday, String birthyear, String profileImage) {
        if (name != null) {
            this.name = name;
        }
        if (birthday != null) {
            this.birthday = birthday;
        }
        if (birthyear != null) {
            this.birthyear = birthyear;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    public void updateGoogleProfile(String name, String profileImage) {
        if (name != null) {
            this.name = name;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    // 추가 게터 메서드들
    public UserGenderType getGender() {
        return this.userGenderType;
    }

    public String getEmail() {
        // OAuth 로그인에서 email 정보는 userIdentity에 저장됨
        return this.userIdentity;
    }

    public String getBirthyear() {
        return this.birthyear;
    }

    public void setGender(UserGenderType gender) {
        this.userGenderType = gender;
    }

    public void setBirthyear(Integer birthyear) {
        this.birthyear = birthyear != null ? birthyear.toString() : null;
    }

    public void setBirthyear(String birthyear) {
        this.birthyear = birthyear;
    }

    public void setIsOnboardingCompleted(Boolean isOnboardingCompleted) {
        this.isOnboardingCompleted = isOnboardingCompleted;
    }
}
