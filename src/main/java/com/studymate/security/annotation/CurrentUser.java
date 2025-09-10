package com.studymate.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자의 UUID를 주입받기 위한 커스텀 어노테이션
 * 
 * @AuthenticationPrincipal을 래핑하여 가독성을 높임
 * Spring Security의 Authentication Principal에서 UUID를 추출
 * 
 * @since 2025-09-10
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUser {
}