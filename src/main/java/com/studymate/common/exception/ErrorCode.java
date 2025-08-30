package com.studymate.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    // 공통 에러 (1000~1999)
    INVALID_REQUEST(1000, "잘못된 요청입니다"),
    UNAUTHORIZED(1001, "인증이 필요합니다"),
    FORBIDDEN(1002, "접근 권한이 없습니다"),
    INTERNAL_SERVER_ERROR(1003, "서버 내부 오류가 발생했습니다"),
    
    // 인증 관련 에러 (2000~2999)
    INVALID_TOKEN(2000, "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(2001, "토큰이 만료되었습니다"),
    OAUTH_ERROR(2002, "OAuth 인증 중 오류가 발생했습니다"),
    LOGIN_FAILED(2003, "로그인에 실패했습니다"),
    
    // 사용자 관련 에러 (3000~3999)
    USER_NOT_FOUND(3000, "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(3001, "이미 존재하는 사용자입니다"),
    INVALID_USER_DATA(3002, "사용자 데이터가 유효하지 않습니다"),
    PROFILE_UPDATE_FAILED(3003, "프로필 업데이트에 실패했습니다"),
    
    // 채팅 관련 에러 (4000~4999)
    CHAT_ROOM_NOT_FOUND(4000, "채팅방을 찾을 수 없습니다"),
    CHAT_ROOM_FULL(4001, "채팅방 인원이 가득 찼습니다"),
    MESSAGE_SEND_FAILED(4002, "메시지 전송에 실패했습니다"),
    WEBSOCKET_CONNECTION_ERROR(4003, "WebSocket 연결 오류가 발생했습니다"),
    
    // 온보딩 관련 에러 (5000~5999)
    ONBOARDING_NOT_FOUND(5000, "온보딩 정보를 찾을 수 없습니다"),
    ONBOARDING_ALREADY_COMPLETED(5001, "이미 온보딩이 완료되었습니다"),
    INVALID_ONBOARDING_DATA(5002, "온보딩 데이터가 유효하지 않습니다"),
    
    // 매칭 관련 에러 (6000~6999)
    MATCHING_FAILED(6000, "매칭에 실패했습니다"),
    NO_AVAILABLE_MATCHES(6001, "매칭 가능한 상대가 없습니다"),
    MATCHING_ALREADY_EXISTS(6002, "이미 매칭이 진행 중입니다"),
    
    // 파일 관련 에러 (7000~7999)
    FILE_UPLOAD_FAILED(7000, "파일 업로드에 실패했습니다"),
    INVALID_FILE_FORMAT(7001, "지원하지 않는 파일 형식입니다"),
    FILE_SIZE_EXCEEDED(7002, "파일 크기가 초과되었습니다"),
    
    // 외부 API 관련 에러 (8000~8999)
    EXTERNAL_API_ERROR(8000, "외부 API 호출 중 오류가 발생했습니다"),
    NAVER_API_ERROR(8001, "Naver API 오류"),
    CLOVA_API_ERROR(8002, "Clova API 오류");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}