package com.studymate.domain.user.util;

import com.studymate.domain.user.domain.dao.UserDao;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// JwtAuthenticationFilter.java

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDao userDao;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDao userDao) {
        this.jwtUtils = jwtUtils;
        this.userDao = userDao;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String token = jwtUtils.resolveToken(request);

        if (token != null && jwtUtils.validateToken(token)) {
            try {
                UUID userId = jwtUtils.getUserIdFromToken(token);
                User user = userDao.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException("USER NOT FOUND"));

                CustomUserDetails userDetails =
                        new CustomUserDetails(user.getUserId(), user.getName());
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ex) {
                // 인증 실패 → 즉시 401 Unauthorized 응답
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("api/login")
                || path.startsWith("api/auth")
                //헬스체크 허용
                || path.equals("health");
    }
}

