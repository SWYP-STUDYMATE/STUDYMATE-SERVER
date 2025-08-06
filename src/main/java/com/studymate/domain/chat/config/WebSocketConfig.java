package com.studymate.domain.chat.config;

import com.studymate.common.exception.StudymateExceptionType;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.domain.user.util.CustomUserDetails;
import com.studymate.domain.user.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepo;

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(1024 * 1024); // 1MB로 설정
        registration.setTimeToFirstMessage(30000); // 30초
        registration.setSendTimeLimit(20000); // 20초
        registration.setSendBufferSizeLimit(512 * 1024); // 512KB
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
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
                    // 1) 헤더 이름이 Authorization 또는 authorization
                    String bearer = accessor.getFirstNativeHeader("Authorization");
                    if (bearer == null) {
                        bearer = accessor.getFirstNativeHeader("authorization");
                    }
                    // 2) Bearer 토큰 파싱
                    if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                        String token = bearer.substring(7);
                        if (jwtUtils.validateToken(token)) {
                            UUID userId = jwtUtils.getUserIdFromToken(token);
                            User user = userRepo.findById(userId)
                                    .orElseThrow(() -> StudymateExceptionType.NOT_FOUND_USER.of());

                            CustomUserDetails userDetails =
                                    new CustomUserDetails(user.getUserId(), user.getName());
                            var auth = new org.springframework.security.authentication
                                    .UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                            // STOMP 세션에 Principal 로 붙여 줌
                            accessor.setUser(auth);
                        }
                    }
                }
                return message;
            }
        });
    }
}
