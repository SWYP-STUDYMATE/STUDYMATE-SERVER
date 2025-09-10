package com.studymate.domain.onboarding.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.studymate.domain.onboarding.entity.Language;
import lombok.Builder;

/**
 * 언어 정보 응답 DTO - 클라이언트 정합성 개선 버전
 * 
 * 클라이언트 인터페이스:
 * export interface LanguageResponse {
 *   id: number;
 *   name: string;
 *   code: string;
 *   flag?: string;
 * }
 * 
 * @since 2025-09-10
 */
@Builder
public record LanguageResponse(
        @JsonProperty("id")
        int id,  // languageId → id로 변경
        
        @JsonProperty("name")
        String name,  // languageName → name으로 변경
        
        @JsonProperty("code")
        String code,  // 언어 코드 추가
        
        @JsonProperty("flag")
        String flag   // 국기 이모지 추가
) {
    
    /**
     * 기존 생성자와의 호환성 유지
     */
    public LanguageResponse(int languageId, String languageName) {
        this(languageId, languageName, null, null);
    }
    
    /**
     * Language 엔티티를 LanguageResponse로 변환
     */
    public static LanguageResponse from(Language language) {
        if (language == null) {
            return null;
        }
        
        String languageCode = language.getLanguageCode();
        
        return new LanguageResponse(
                language.getLanguageId(),
                language.getLanguageName(),
                languageCode,
                getFlagEmoji(languageCode)
        );
    }
    
    /**
     * 언어 코드에 따른 국기 이모지 반환
     */
    private static String getFlagEmoji(String languageCode) {
        if (languageCode == null) {
            return null;
        }
        
        return switch (languageCode.toLowerCase()) {
            case "ko", "kr" -> "🇰🇷";  // 한국어
            case "en", "us" -> "🇺🇸";  // 영어
            case "jp", "ja" -> "🇯🇵";  // 일본어
            case "cn", "zh" -> "🇨🇳";  // 중국어
            case "es" -> "🇪🇸";       // 스페인어
            case "fr" -> "🇫🇷";       // 프랑스어
            case "de" -> "🇩🇪";       // 독일어
            case "it" -> "🇮🇹";       // 이탈리아어
            case "pt" -> "🇵🇹";       // 포르투갈어
            case "ru" -> "🇷🇺";       // 러시아어
            default -> null;
        };
    }
    
    // 기존 코드와의 호환성을 위한 getter 메서드
    public int getLanguageId() {
        return this.id;
    }
    
    public String getLanguageName() {
        return this.name;
    }
}
