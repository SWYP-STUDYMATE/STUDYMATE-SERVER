package com.studymate.domain.user.util;

import com.studymate.domain.user.domain.dto.response.LoginTokenResponse;
import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import com.studymate.exception.NotFoundException;
import io.jsonwebtoken.lang.Arrays;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils,UserRepository userRepository){
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {

        String token = jwtUtils.resolveToken(request);

        if (token != null && !jwtUtils.isTokenExpired(token)){
            LoginTokenResponse loginTokenResponse = jwtUtils.parseLoginToken(token);
            User user = userRepository.findById(loginTokenResponse.uuid())
                    .orElseThrow(()-> new NotFoundException("USER NOT FOUND"));
            CustomUserDetails userDetails = new CustomUserDetails(user.getUserId(), user.getName());
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request,response);



    }






}
