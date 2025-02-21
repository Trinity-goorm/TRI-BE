package com.trinity.ctc.kakao.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.trinity.ctc.domain.user.entity.User;
import java.util.Optional;

import com.trinity.ctc.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

//@SpringBootTest
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserRepositoryTest {

    @Mock //UserRepository를 Mock으로 설정
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito 초기화
    }

    @Test
    public void testFindByKakaoId_WhenUserExists() {
        // Given (가짜 데이터 설정)
        User mockUser = User.builder().kakaoId(123456789L).build();

        when(userRepository.findByKakaoId(123456789L)).thenReturn(Optional.of(mockUser));

        // When
        Optional<User> foundUser = userRepository.findByKakaoId(123456789L);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals(123456789L, foundUser.get().getKakaoId());

    }
    @Test
    public void testFindByKakaoId_WhenUserDoesNotExist() {
        // Given (DB에 해당 kakaoId가 없음)
        when(userRepository.findByKakaoId(987654321L)).thenReturn(Optional.empty());

        // When
        Optional<User> foundUser = userRepository.findByKakaoId(987654321L);

        // Then
        assertFalse(foundUser.isPresent());
    }
}