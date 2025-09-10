package com.studymate.domain.user.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.studymate.domain.user.domain.type.UserGenderType;
import lombok.Builder;

/**
 * 사용자 성별 타입 응답 DTO - 클라이언트 정합성 개선 버전
 * 
 * 클라이언트 인터페이스:
 * export interface UserGenderTypeResponse {
 *   id: number;
 *   name: string;
 *   code: string;
 * }
 * 
 * @since 2025-09-10
 */
@Builder
public record UserGenderTypeResponse(
        @JsonProperty("id")
        int id,
        
        @JsonProperty("name")
        String name,
        
        @JsonProperty("code")
        String code
) {
    
    /**
     * 기존 생성자와의 호환성 유지
     */
    public UserGenderTypeResponse(String name, String description) {
        this(0, name, description);
    }
    
    /**
     * UserGenderType enum을 UserGenderTypeResponse로 변환
     */
    public static UserGenderTypeResponse from(UserGenderType genderType) {
        if (genderType == null) {
            return null;
        }
        
        return UserGenderTypeResponse.builder()
                .id(genderType.ordinal() + 1)  // enum ordinal을 ID로 사용 (1부터 시작)
                .name(getGenderName(genderType))
                .code(genderType.name())
                .build();
    }
    
    /**
     * 성별 타입에 대한 표시명 반환
     */
    private static String getGenderName(UserGenderType genderType) {
        return switch (genderType) {
            case MALE -> "남성";
            case FEMALE -> "여성";
            case OTHER -> "기타";
            case PREFER_NOT_TO_SAY -> "밝히지 않음";
            default -> genderType.name();
        };
    }
    
    /**
     * ID로부터 UserGenderType enum 역변환
     */
    public static UserGenderType toGenderType(int id) {
        UserGenderType[] values = UserGenderType.values();
        if (id > 0 && id <= values.length) {
            return values[id - 1];
        }
        return null;
    }
    
    // 기존 코드와의 호환성을 위한 getter
    public String getDescription() {
        return this.code;
    }
}
