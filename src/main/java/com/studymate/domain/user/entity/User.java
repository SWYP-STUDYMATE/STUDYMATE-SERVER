package com.studymate.domain.user.entity;

import com.studymate.domain.onboarding.domain.type.CommunicationMethodType;
import com.studymate.domain.onboarding.domain.type.DailyMinuteType;
import com.studymate.domain.onboarding.domain.type.PartnerGenderType;
import com.studymate.domain.onboarding.entity.Language;
import com.studymate.domain.onboarding.domain.type.LearningExpectionType;
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
    private String gender;

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






    public void updateNaverProfile(String name, String birthday,String gender,String birthyear, String profileImage){
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.birthyear = birthyear;
        this.profileImage = profileImage;

    }




}
