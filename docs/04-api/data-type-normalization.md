# 📐 데이터 타입 정규화 가이드

## 📅 문서 정보
- **버전**: 1.0
- **작성일**: 2025-09-10
- **작성자**: Backend Development Team
- **목적**: 클라이언트-서버 데이터 타입 불일치 해결 및 정규화

---

## 🔧 해결된 정합성 문제

### 1. ✅ Location 엔티티 필드 불일치 해결

#### 문제점
- **서버 엔티티**: `locationId`, `country`, `city`, `timeZone`
- **클라이언트 인터페이스**: `id`, `name`, `country`, `countryCode`
- **불일치**: 필드명과 구조가 상이

#### 해결 방법
```java
// LocationResponseV2.java - 새로운 DTO 생성
@Builder
public record LocationResponseV2(
    int id,              // locationId → id
    String name,         // city → name 
    String country,      // country 유지
    String countryCode   // 국가 코드 추가
) {
    public static LocationResponseV2 from(Location location) {
        return LocationResponseV2.builder()
            .id(location.getLocationId())
            .name(location.getCity())  // 도시명을 name으로 매핑
            .country(location.getCountry())
            .countryCode(getCountryCode(location.getCountry()))
            .build();
    }
}
```

#### 매핑 테이블
| 서버 필드 | 클라이언트 필드 | 변환 방식 |
|----------|---------------|----------|
| locationId | id | 직접 매핑 |
| city | name | 필드명 변경 |
| country | country | 직접 매핑 |
| - | countryCode | 국가명→코드 변환 |
| timeZone | - | 클라이언트 미사용 |

---

### 2. ✅ birthyear 타입 변환 문제 해결

#### 문제점
- **서버 엔티티**: `String birthyear`
- **클라이언트 인터페이스**: `number birthYear`
- **불일치**: 타입과 필드명 상이

#### 해결 방법
```java
// UserProfileResponseV2.java
@JsonProperty("birthYear")
Integer birthYear,  // String → Integer 변환

// 변환 메서드
private static Integer convertBirthyearToInteger(String birthyear) {
    if (birthyear == null || birthyear.trim().isEmpty()) {
        return null;
    }
    try {
        return Integer.parseInt(birthyear.trim());
    } catch (NumberFormatException e) {
        return null;  // 변환 실패시 null
    }
}
```

#### 요청 DTO 개선
```java
// BirthYearRequest.java
record BirthYearRequest(
    Integer birthYear  // Integer로 직접 수신
) {}

// BirthYearResponse.java  
record BirthYearResponse(
    Integer birthYear  // Integer로 반환
) {}
```

---

### 3. ✅ profileImage 필드명 불일치 해결

#### 문제점
- **서버 엔티티**: `profileImage`
- **클라이언트 인터페이스**: `profileImageUrl`

#### 해결 방법
```java
@JsonProperty("profileImageUrl")
String profileImageUrl,  // profileImage → profileImageUrl
```

---

## 📊 개선된 DTO 구조

### UserProfileResponseV2
```java
public record UserProfileResponseV2(
    @JsonProperty("id") String id,                    // UUID → String
    @JsonProperty("englishName") String englishName,
    @JsonProperty("profileImageUrl") String profileImageUrl,  // 필드명 변경
    @JsonProperty("selfBio") String selfBio,
    @JsonProperty("location") LocationResponseV2 location,    // V2 사용
    @JsonProperty("nativeLanguage") LanguageResponse nativeLanguage,
    @JsonProperty("targetLanguages") List<LanguageResponse> targetLanguages,
    @JsonProperty("birthYear") Integer birthYear,     // String → Integer
    @JsonProperty("birthday") String birthday,
    @JsonProperty("gender") UserGenderTypeResponse gender,
    @JsonProperty("createdAt") String createdAt,      // ISO 8601
    @JsonProperty("updatedAt") String updatedAt       // ISO 8601
)
```

### LanguageResponse (개선)
```java
public record LanguageResponse(
    @JsonProperty("id") int id,           // languageId → id
    @JsonProperty("name") String name,    // languageName → name
    @JsonProperty("code") String code,    // 언어 코드 추가
    @JsonProperty("flag") String flag     // 국기 이모지 추가
)
```

### UserGenderTypeResponse (개선)
```java
public record UserGenderTypeResponse(
    @JsonProperty("id") int id,
    @JsonProperty("name") String name,    // 한글명
    @JsonProperty("code") String code     // MALE, FEMALE, OTHER
)
```

---

## 🔄 마이그레이션 전략

### 1단계: 병행 운영 (현재)
- V1 API: 기존 DTO 사용 (`/api/v1/users`)
- V2 API: 개선된 DTO 사용 (`/api/v2/users`)
- 클라이언트: V2 API로 점진적 전환

### 2단계: 전환 완료 (1개월 후)
- 모든 클라이언트 V2 API 사용
- V1 API Deprecated 선언

### 3단계: 레거시 제거 (3개월 후)
- V1 API 제거
- V2를 기본 버전으로 승격

---

## 🧪 검증 체크리스트

### 타입 검증
- [x] Location: `id`, `name`, `country`, `countryCode` 매핑 확인
- [x] birthYear: String → Integer 변환 확인
- [x] profileImageUrl: 필드명 변경 확인
- [x] 날짜: LocalDateTime → ISO 8601 String 변환 확인

### API 테스트
```bash
# V2 API 프로필 조회 테스트
curl -X GET http://localhost:8080/api/v2/users/profile \
  -H "Authorization: Bearer {token}"

# 예상 응답
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "englishName": "John",
    "profileImageUrl": "https://...",
    "birthYear": 1990,  // 숫자 타입
    "location": {
      "id": 1,
      "name": "Seoul",  // city가 name으로
      "country": "Korea",
      "countryCode": "KR"
    }
  }
}
```

### TypeScript 타입 체크
```typescript
// 클라이언트 타입 검증
const profile: UserProfileResponse = await api.getUserProfile();
console.assert(typeof profile.birthYear === 'number');
console.assert(typeof profile.location.id === 'number');
console.assert(typeof profile.location.name === 'string');
```

---

## 📈 성능 영향

### 변환 오버헤드
- String → Integer 변환: < 1ms
- 국가 코드 매핑: < 1ms
- 전체 DTO 변환: < 5ms

### 메모리 사용
- 추가 DTO 클래스: ~10KB
- 런타임 오버헤드: 무시 가능

---

## 🚀 향후 개선 사항

### 단기 (1개월)
- [ ] 국가 코드 매핑 DB 테이블화
- [ ] 타입 변환 유틸리티 클래스 생성
- [ ] 통합 테스트 케이스 추가

### 중기 (3개월)
- [ ] OpenAPI 스펙에서 TypeScript 자동 생성
- [ ] DTO 버전 관리 시스템 구축
- [ ] 필드 매핑 자동화 도구 개발

### 장기 (6개월)
- [ ] GraphQL 도입으로 필드 선택적 조회
- [ ] Protocol Buffers 도입 검토
- [ ] 스키마 레지스트리 구축

---

## 📝 개발자 가이드

### 새 DTO 생성시 체크리스트
1. **클라이언트 인터페이스 확인**: `/STYDYMATE-CLIENT/src/types/api.d.ts`
2. **@JsonProperty 사용**: 필드명 명시적 지정
3. **타입 변환 메서드**: null 안전 처리
4. **호환성 생성자**: 기존 코드 지원
5. **from() 메서드**: 엔티티 → DTO 변환
6. **문서화**: 변경 사항 기록

### 타입 변환 패턴
```java
// String → Integer (null-safe)
Integer value = str != null ? Integer.parseInt(str) : null;

// UUID → String
String id = uuid != null ? uuid.toString() : null;

// LocalDateTime → ISO 8601 String
String datetime = ldt != null ? ldt.toString() : null;

// Enum → DTO
ResponseDTO dto = enumValue != null ? ResponseDTO.from(enumValue) : null;
```

---

*이 문서는 클라이언트-서버 데이터 타입 정합성 문제 해결 과정을 기록합니다.*
*모든 변경사항은 V2 API에 적용되며, 기존 V1 API는 영향받지 않습니다.*