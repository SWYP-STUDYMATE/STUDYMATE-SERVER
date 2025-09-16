package com.studymate.domain.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class NotificationHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 연결 요청 로깅
        log.info("WebSocket handshake request from: {}", request.getRemoteAddress());

        // 쿼리 파라미터에서 토큰 추출 (SockJS용)
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = extractTokenFromQuery(query);
            if (token != null) {
                attributes.put("token", token);
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception == null) {
            log.info("WebSocket handshake successful");
        } else {
            log.error("WebSocket handshake failed: {}", exception.getMessage());
        }
    }

    private String extractTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }
}