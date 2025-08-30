package com.studymate.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    @DisplayName("공개 경로는 인증 없이 접근 가능하다")
    void publicEndpoints_AccessibleWithoutAuth() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // Health check는 공개되어야 함
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());

        // Swagger는 공개되어야 함
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); // 리다이렉션은 정상
    }

    @Test
    @DisplayName("보호된 경로는 인증 없이 접근 불가능하다")
    void protectedEndpoints_RequireAuth() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // 보호된 사용자 API는 인증이 필요
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isUnauthorized());
    }
}