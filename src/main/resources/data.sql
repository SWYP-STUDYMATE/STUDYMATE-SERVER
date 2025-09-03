-- Initial data for STUDYMATE-SERVER
-- This file will be executed automatically by Spring Boot on startup

-- MOTIVATION 테이블 초기 데이터
INSERT INTO MOTIVATION (motivation_name) VALUES ('취업/진학');
INSERT INTO MOTIVATION (motivation_name) VALUES ('자기 개발');
INSERT INTO MOTIVATION (motivation_name) VALUES ('취미 생활');
INSERT INTO MOTIVATION (motivation_name) VALUES ('여행/문화 교류');
INSERT INTO MOTIVATION (motivation_name) VALUES ('자존감 향상');
INSERT INTO MOTIVATION (motivation_name) VALUES ('영어 시험 준비 (TOEIC, IELTS, OPIC 등)');
INSERT INTO MOTIVATION (motivation_name) VALUES ('친구 사귀기 또는 대화');
INSERT INTO MOTIVATION (motivation_name) VALUES ('유학/어학연수 준비');
INSERT INTO MOTIVATION (motivation_name) VALUES ('이민 준비');

-- TOPIC 테이블 초기 데이터
INSERT INTO TOPIC (topic_name) VALUES ('여행');
INSERT INTO TOPIC (topic_name) VALUES ('시사');
INSERT INTO TOPIC (topic_name) VALUES ('음식');
INSERT INTO TOPIC (topic_name) VALUES ('비즈니스');
INSERT INTO TOPIC (topic_name) VALUES ('드라마/영화');
INSERT INTO TOPIC (topic_name) VALUES ('문화/라이프스타일');
INSERT INTO TOPIC (topic_name) VALUES ('운동/스포츠');
INSERT INTO TOPIC (topic_name) VALUES ('경제');
INSERT INTO TOPIC (topic_name) VALUES ('IT');

-- LEARNING_STYLE 테이블 초기 데이터
INSERT INTO LEARNING_STYLE (learning_style_name) VALUES ('듣는 걸 좋아해요');
INSERT INTO LEARNING_STYLE (learning_style_name) VALUES ('말하는 걸 좋아해요');
INSERT INTO LEARNING_STYLE (learning_style_name) VALUES ('둘 다 반반이예요');

-- Location 테이블 초기 데이터
INSERT INTO Location (country, city, timezone) VALUES
('대한민국', '서울', 'GMT+9:00'),
('중국', '베이징', 'GMT+8:00'),
('일본', '도쿄', 'GMT+9:00'),
('독일', '베를린', 'GMT+1:00'),
('영국', '런던', 'GMT+0:00'),
('프랑스', '파리', 'GMT+1:00'),
('미국', '뉴욕', 'GMT-5:00'),
('태국', '방콕', 'GMT+7:00'),
('미국', '로스앤젤레스', 'GMT-8:00'),
('호주', '시드니', 'GMT+10:00'),
('터키', '이스탄불', 'GMT+3:00'),
('아랍에미리트', '두바이', 'GMT+4:00');

-- Language 테이블 초기 데이터
INSERT INTO Language (language_name) VALUES
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

-- PARTNER_PARSONALITY 테이블 초기 데이터
INSERT INTO PARTNER_PARSONALITY (partner_personality) VALUES ('편하게 대화 나누는 친구 같은 파트너');
INSERT INTO PARTNER_PARSONALITY (partner_personality) VALUES ('교정과 피드백을 잘 주는 선생님 스타일');
INSERT INTO PARTNER_PARSONALITY (partner_personality) VALUES ('함께 주제를 정해 대화할 수 있는 파트너');
INSERT INTO PARTNER_PARSONALITY (partner_personality) VALUES ('자신과 비슷한 관심사를 가지고 있는 파트너');
INSERT INTO PARTNER_PARSONALITY (partner_personality) VALUES ('서로 도움을 주며 같이 성장할 수 있는 파트너');

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