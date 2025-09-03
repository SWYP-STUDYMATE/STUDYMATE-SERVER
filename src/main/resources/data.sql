-- Initial data for STUDYMATE-SERVER
-- This file will be executed automatically by Spring Boot on startup

-- MOTIVATION 테이블 초기 데이터
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('취업/진학');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('자기 개발');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('취미 생활');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('여행/문화 교류');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('자존감 향상');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('영어 시험 준비 (TOEIC, IELTS, OPIC 등)');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('친구 사귀기 또는 대화');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('유학/어학연수 준비');
INSERT INTO MOTIVATION (MOTIVATION_NAME) VALUES ('이민 준비');

-- TOPIC 테이블 초기 데이터
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('여행');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('시사');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('음식');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('비즈니스');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('드라마/영화');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('문화/라이프스타일');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('운동/스포츠');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('경제');
INSERT INTO TOPIC (TOPIC_NAME) VALUES ('IT');

-- LEARNING_STYLE 테이블 초기 데이터
INSERT INTO LEARNING_STYLE (LEARNING_STYLE_NAME) VALUES ('듣는 걸 좋아해요');
INSERT INTO LEARNING_STYLE (LEARNING_STYLE_NAME) VALUES ('말하는 걸 좋아해요');
INSERT INTO LEARNING_STYLE (LEARNING_STYLE_NAME) VALUES ('둘 다 반반이예요');

-- Location 테이블 초기 데이터 (onboarding-info 페이지에서 활용)
INSERT INTO LOCATION (COUNTRY, CITY, TIMEZONE) VALUES
-- 아시아/태평양
('대한민국', '서울', 'Asia/Seoul'),
('일본', '도쿄', 'Asia/Tokyo'),
('중국', '베이징', 'Asia/Shanghai'),
('중국', '상하이', 'Asia/Shanghai'),
('대만', '타이베이', 'Asia/Taipei'),
('홍콩', '홍콩', 'Asia/Hong_Kong'),
('싱가포르', '싱가포르', 'Asia/Singapore'),
('태국', '방콕', 'Asia/Bangkok'),
('베트남', '호치민', 'Asia/Ho_Chi_Minh'),
('베트남', '하노이', 'Asia/Ho_Chi_Minh'),
('말레이시아', '쿠알라룸푸르', 'Asia/Kuala_Lumpur'),
('필리핀', '마닐라', 'Asia/Manila'),
('인도네시아', '자카르타', 'Asia/Jakarta'),
('인도', '뉴델리', 'Asia/Kolkata'),
('인도', '뭄바이', 'Asia/Kolkata'),
('호주', '시드니', 'Australia/Sydney'),
('호주', '멜버른', 'Australia/Melbourne'),
('뉴질랜드', '오클랜드', 'Pacific/Auckland'),

-- 북미
('미국', '뉴욕', 'America/New_York'),
('미국', '로스앤젤레스', 'America/Los_Angeles'),
('미국', '시카고', 'America/Chicago'),
('미국', '시애틀', 'America/Los_Angeles'),
('미국', '샌프란시스코', 'America/Los_Angeles'),
('캐나다', '토론토', 'America/Toronto'),
('캐나다', '밴쿠버', 'America/Vancouver'),

-- 유럽
('영국', '런던', 'Europe/London'),
('프랑스', '파리', 'Europe/Paris'),
('독일', '베를린', 'Europe/Berlin'),
('독일', '프랑크푸르트', 'Europe/Berlin'),
('이탈리아', '로마', 'Europe/Rome'),
('스페인', '마드리드', 'Europe/Madrid'),
('네덜란드', '암스테르담', 'Europe/Amsterdam'),
('스위스', '취리히', 'Europe/Zurich'),
('오스트리아', '비엔나', 'Europe/Vienna'),
('러시아', '모스크바', 'Europe/Moscow'),
('러시아', '상트페테르부르크', 'Europe/Moscow'),

-- 중동/아프리카
('터키', '이스탄불', 'Europe/Istanbul'),
('아랍에미리트', '두바이', 'Asia/Dubai'),
('사우디아라비아', '리야드', 'Asia/Riyadh'),
('이스라엘', '텔아비브', 'Asia/Jerusalem'),
('남아프리카공화국', '케이프타운', 'Africa/Johannesburg'),
('이집트', '카이로', 'Africa/Cairo'),

-- 남미
('브라질', '상파울루', 'America/Sao_Paulo'),
('브라질', '리우데자네이루', 'America/Sao_Paulo'),
('아르헨티나', '부에노스아이레스', 'America/Argentina/Buenos_Aires'),
('멕시코', '멕시코시티', 'America/Mexico_City');

-- Language 테이블 초기 데이터  
INSERT INTO LANGUAGE (LANGUAGE_NAME) VALUES
('한국어'),
('영어'),
('일본어'),
('중국어'),
('프랑스어'),
('독일어'),
('스페인어'),
('러시아어'),
('베트남어'),
('태국어'),
('아랍어'),
('포르투갈어');

-- PARTNER_PERSONALITY 테이블 초기 데이터 (오타 수정: PARSONALITY -> PERSONALITY)
INSERT INTO PARTNER_PERSONALITY (PARTNER_PERSONALITY) VALUES ('편하게 대화 나누는 친구 같은 파트너');
INSERT INTO PARTNER_PERSONALITY (PARTNER_PERSONALITY) VALUES ('교정과 피드백을 잘 주는 선생님 스타일');
INSERT INTO PARTNER_PERSONALITY (PARTNER_PERSONALITY) VALUES ('함께 주제를 정해 대화할 수 있는 파트너');
INSERT INTO PARTNER_PERSONALITY (PARTNER_PERSONALITY) VALUES ('자신과 비슷한 관심사를 가지고 있는 파트너');
INSERT INTO PARTNER_PERSONALITY (PARTNER_PERSONALITY) VALUES ('서로 도움을 주며 같이 성장할 수 있는 파트너');

-- LANG_LEVEL_TYPE 테이블 초기 데이터 (학습 언어 레벨)
INSERT INTO LANG_LEVEL_TYPE (LANG_LEVEL_ID, LANG_LEVEL_NAME) VALUES
(100, '간단한 일상 대화를 할 수 있어요'),
(101, '일상 주제는 끊김 없이 대화할 수 있어요'),
(102, '관심사, 시사 등 조금 복잡한 주제로도 말할 수 있어요'),
(103, '의견 설명, 비교 등 심화 대화도 자연스러워요'),
(104, '현지에서 살면서 자연스럽게 말해요'),
(105, '해당 언어로 대학을 졸업해 전문 어휘도 활용할 수 있어요'),
(106, '거의 네이티브처럼 말할 수 있어요');

-- LANG_LEVEL_TYPE 테이블 초기 데이터 (모국어 레벨)
INSERT INTO LANG_LEVEL_TYPE (LANG_LEVEL_ID, LANG_LEVEL_NAME) VALUES
(200, '일상 대화를 편하게 이어갈 수 있어요'),
(201, '복잡한 주제도 자연스럽게 이야기할 수 있어요'),
(202, '실제 원어민과 자연스러운 대화를 하고 싶어요'),
(203, '나와 비슷한 실력으로 서로 연습할 수 있어요');

-- GROUP_SIZE 테이블 초기 데이터
INSERT INTO GROUP_SIZE (GROUP_SIZE) VALUES
('1:1'),
('3명'),
('4명'),
('상관없음');

-- 온보딩 페이지에서 사용되는 추가 데이터들

-- Communication Method (소통 방식) - OnboardScheduleController에서 사용
-- 참고: CommunicationMethodType enum
-- 실제 테이블이 있는지 확인 필요 (Enum으로만 관리될 수도 있음)

-- Daily Minute (일일 학습 시간) - OnboardScheduleController에서 사용  
-- 참고: DailyMinuteType enum
-- 실제 테이블이 있는지 확인 필요 (Enum으로만 관리될 수도 있음)

-- Partner Gender Preference (파트너 성별 선호도) - OnboardPartnerController에서 사용
-- 참고: PartnerGenderType enum  
-- 실제 테이블이 있는지 확인 필요 (Enum으로만 관리될 수도 있음)

-- Learning Expectation (학습 기대값) - OnbaordInterestController에서 사용
-- 참고: LearningExpectionType enum
-- 실제 테이블이 있는지 확인 필요 (Enum으로만 관리될 수도 있음)

-- SCHEDULE 테이블 초기 데이터 (온보딩에서 스케줄 선택용)
INSERT INTO SCHEDULE (DAY_OF_WEEK, SCHEDULE_NAME, TIME_SLOT) VALUES
('MONDAY', '월요일 오전', '09:00-12:00'),
('MONDAY', '월요일 오후', '13:00-18:00'),
('MONDAY', '월요일 저녁', '19:00-22:00'),
('TUESDAY', '화요일 오전', '09:00-12:00'),
('TUESDAY', '화요일 오후', '13:00-18:00'),
('TUESDAY', '화요일 저녁', '19:00-22:00'),
('WEDNESDAY', '수요일 오전', '09:00-12:00'),
('WEDNESDAY', '수요일 오후', '13:00-18:00'),
('WEDNESDAY', '수요일 저녁', '19:00-22:00'),
('THURSDAY', '목요일 오전', '09:00-12:00'),
('THURSDAY', '목요일 오후', '13:00-18:00'),
('THURSDAY', '목요일 저녁', '19:00-22:00'),
('FRIDAY', '금요일 오전', '09:00-12:00'),
('FRIDAY', '금요일 오후', '13:00-18:00'),
('FRIDAY', '금요일 저녁', '19:00-22:00'),
('SATURDAY', '토요일 오전', '09:00-12:00'),
('SATURDAY', '토요일 오후', '13:00-18:00'),
('SATURDAY', '토요일 저녁', '19:00-22:00'),
('SUNDAY', '일요일 오전', '09:00-12:00'),
('SUNDAY', '일요일 오후', '13:00-18:00'),
('SUNDAY', '일요일 저녁', '19:00-22:00');