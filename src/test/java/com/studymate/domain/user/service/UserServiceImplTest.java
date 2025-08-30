package com.studymate.domain.user.service;

import com.studymate.domain.user.domain.repository.UserRepository;
import com.studymate.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    private UUID testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
            .userId(testUserId)
            .userIdentity("test@example.com")
            .name("TestUser")
            .englishName("John Doe")
            .build();
    }

    @Test
    @DisplayName("사용자 조회 테스트")
    void findUserById_Success() {
        // given
        given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));

        // when
        Optional<User> result = userRepository.findById(testUserId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getName()).isEqualTo("TestUser");
        assertThat(result.get().getEnglishName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 테스트")
    void findUserById_NotFound() {
        // given
        given(userRepository.findById(testUserId)).willReturn(Optional.empty());

        // when
        Optional<User> result = userRepository.findById(testUserId);

        // then
        assertThat(result).isEmpty();
    }
}