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
@Table(name = "USER")
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




}
