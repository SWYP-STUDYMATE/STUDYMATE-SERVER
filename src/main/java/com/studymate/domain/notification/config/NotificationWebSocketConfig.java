package com.studymate.domain.notification.config;

import com.studymate.auth.jwt.JwtUtils;
import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.util.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.UUID;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class NotificationWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(512 * 1024); // 512KB
        registration.setTimeToFirstMessage(30000); // 30초
        registration.setSendTimeLimit(20000); // 20초
        registration.setSendBufferSizeLimit(256 * 1024); // 256KB
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 알림 전용 WebSocket 엔드포인트
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setInterceptors(new NotificationHandshakeInterceptor());

        // SockJS 없이 순수 WebSocket도 지원
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트 구독 경로
        registry.enableSimpleBroker(
            "/sub",                      // 일반 구독
            "/user",                     // 개인 메시지
            "/topic"                     // 브로드캐스트
        );

        // 클라이언트 발행 경로
        registry.setApplicationDestinationPrefixes("/pub");

        // 사용자별 목적지 접두사
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // JWT 토큰 처리
                    String authToken = extractToken(accessor);

                    if (StringUtils.hasText(authToken)) {
                        try {
                            if (jwtUtils.validateToken(authToken)) {
                                UUID userId = jwtUtils.getUserIdFromToken(authToken);
                                User user = userRepository.findById(userId)
                                    .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());

                                CustomUserDetails userDetails =
                                    new CustomUserDetails(user.getUserId(), user.getName());

                                UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                    );

                                accessor.setUser(auth);
                                log.info("WebSocket authentication successful for user: {}", userId);
                            } else {
                                log.warn("Invalid JWT token in WebSocket connection");
                            }
                        } catch (Exception e) {
                            log.error("Error during WebSocket authentication: {}", e.getMessage());
                        }
                    }
                }

                return message;
            }

            private String extractToken(StompHeaderAccessor accessor) {
                // 1. Authorization 헤더에서 추출
                String bearer = accessor.getFirstNativeHeader("Authorization");
                if (bearer == null) {
                    bearer = accessor.getFirstNativeHeader("authorization");
                }

                if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                    return bearer.substring(7);
                }

                // 2. 쿼리 파라미터에서 추출 (SockJS fallback)
                String token = accessor.getFirstNativeHeader("token");
                if (StringUtils.hasText(token)) {
                    return token;
                }

                return null;
            }
        });
    }
}