package com.studymate.domain.user.domain.dto.response;

import com.studymate.domain.user.entity.Location;
import lombok.Builder;

/**
 * 클라이언트와의 정합성을 위한 Location 응답 DTO
 * 
 * 클라이언트 인터페이스:
 * - id: number
 * - name: string (도시명)
 * - country: string
 * - countryCode?: string (optional)
 * 
 * @since 2025-09-10
 */
@Builder
public record LocationResponseV2(
        int id,                    // locationId → id로 매핑
        String name,               // city → name으로 매핑
        String country,            // country 그대로
        String countryCode         // 국가 코드 (KR, US, JP 등)
) {
    /**
     * Entity를 DTO로 변환
     */
    public static LocationResponseV2 from(Location location) {
        if (location == null) {
            return null;
        }
        
        return LocationResponseV2.builder()
                .id(location.getLocationId())
                .name(location.getCity())  // city를 name으로 매핑
                .country(location.getCountry())
                .countryCode(getCountryCode(location.getCountry()))  // 국가명에서 코드 추출
                .build();
    }
    
    /**
     * 국가명에서 국가 코드 추출
     * TODO: 별도의 국가 코드 매핑 테이블 또는 Enum 활용 권장
     */
    private static String getCountryCode(String country) {
        if (country == null) {
            return null;
        }
        
        // 주요 국가 매핑 (확장 가능)
        return switch (country.toLowerCase()) {
            case "korea", "south korea", "대한민국", "한국" -> "KR";
            case "united states", "usa", "미국" -> "US";
            case "japan", "일본" -> "JP";
            case "china", "중국" -> "CN";
            case "united kingdom", "uk", "영국" -> "GB";
            case "canada", "캐나다" -> "CA";
            case "australia", "호주" -> "AU";
            case "germany", "독일" -> "DE";
            case "france", "프랑스" -> "FR";
            case "spain", "스페인" -> "ES";
            default -> null;
        };
    }
}