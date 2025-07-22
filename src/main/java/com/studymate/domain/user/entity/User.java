package com.studymate.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
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

    @Column(name = "NAME")
    private String name;

    @Column(name = "BIRTHDAY")
    private String birthday;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "BIRTHYEAR")
    private String birthyear;

    @Column(name = "ENGLISH_NAME")
    private String englishName;

    @Column(name = "COUNTRY")
    private Enum country;

    @Column(name = "PROFILE_IMAGE")
    private String profileImage;

    @Column(name = "SELF_DESCRIPTION")
    private String selfDescription;

    @Column(name = "USER_DISABLE")
    private boolean userDisable;






    public void updateNaverProfile(String name, String birthday,String gender,String birthyear){
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.birthyear = birthyear;

    }




}
