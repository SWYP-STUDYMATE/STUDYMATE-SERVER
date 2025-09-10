package com.studymate.domain.user.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.studymate.domain.onboarding.domain.dto.response.LanguageResponse;
import com.studymate.domain.user.entity.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 클라이언트와 완벽한 정합성을 위한 UserProfile 응답 DTO
 * 
 * 클라이언트 인터페이스 (api.d.ts):
 * export interface UserProfileResponse {
 *   id: string;
 *   englishName: string;
 *   profileImageUrl?: string;
 *   selfBio?: string;
 *   location?: LocationResponse;
 *   nativeLanguage?: LanguageResponse;
 *   targetLanguages?: LanguageResponse[];
 *   birthYear?: number;  // 숫자 타입
 *   birthday?: string;
 *   gender?: UserGenderTypeResponse;
 *   createdAt: string;
 *   updatedAt: string;
 * }
 * 
 * @since 2025-09-10
 */
@Builder
public record UserProfileResponseV2(
        @JsonProperty("id")
        String id,                              // UUID를 String으로 변환
        
        @JsonProperty("englishName")
        String englishName,
        
        @JsonProperty("profileImageUrl")        // profileImage → profileImageUrl
        String profileImageUrl,
        
        @JsonProperty("selfBio")
        String selfBio,
        
        @JsonProperty("location")
        LocationResponseV2 location,            // LocationResponseV2 사용
        
        @JsonProperty("nativeLanguage")
        LanguageResponse nativeLanguage,
        
        @JsonProperty("targetLanguages")
        List<LanguageResponse> targetLanguages,
        
        @JsonProperty("birthYear")
        Integer birthYear,                       // String → Integer 변환
        
        @JsonProperty("birthday")
        String birthday,
        
        @JsonProperty("gender")
        UserGenderTypeResponse gender,
        
        @JsonProperty("createdAt")
        String createdAt,                        // ISO 8601 형식 문자열
        
        @JsonProperty("updatedAt")
        String updatedAt                         // ISO 8601 형식 문자열
) {
    
    /**
     * User 엔티티를 UserProfileResponseV2로 변환
     * 
     * @param user User 엔티티
     * @param targetLanguages 대상 언어 목록
     * @return UserProfileResponseV2
     */
    public static UserProfileResponseV2 from(User user, List<LanguageResponse> targetLanguages) {
        if (user == null) {
            return null;
        }
        
        return UserProfileResponseV2.builder()
                .id(user.getUserId() != null ? user.getUserId().toString() : null)
                .englishName(user.getEnglishName())
                .profileImageUrl(user.getProfileImage())  // 필드명 매핑
                .selfBio(user.getSelfBio())
                .location(LocationResponseV2.from(user.getLocation()))
                .nativeLanguage(user.getNativeLanguage() != null ? 
                        LanguageResponse.from(user.getNativeLanguage()) : null)
                .targetLanguages(targetLanguages)
                .birthYear(convertBirthyearToInteger(user.getBirthyear()))  // String → Integer
                .birthday(user.getBirthday())
                .gender(user.getGender() != null ? 
                        UserGenderTypeResponse.from(user.getGender()) : null)
                .createdAt(user.getUserCreatedAt() != null ? 
                        user.getUserCreatedAt().toString() : null)
                .updatedAt(LocalDateTime.now().toString())  // 현재 시간 사용
                .build();
    }
    
    /**
     * birthyear String을 Integer로 안전하게 변환
     * 
     * @param birthyear String 형태의 출생년도
     * @return Integer 형태의 출생년도, 변환 실패시 null
     */
    private static Integer convertBirthyearToInteger(String birthyear) {
        if (birthyear == null || birthyear.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Integer.parseInt(birthyear.trim());
        } catch (NumberFormatException e) {
            // 로그 기록 후 null 반환
            // log.warn("Failed to convert birthyear to Integer: {}", birthyear);
            return null;
        }
    }
}