package com.trinity.ctc.domain.fcm.service;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import com.trinity.ctc.global.kakao.service.AuthService;
import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    public final FcmRepository fcmRepository;
    public final UserRepository userRepository;
    public final AuthService authService;

    /**
     * 로그인 시, 해당 기기에 대한 사용자의 fcm 토큰 정보 초기화
     *
     * @param fcmToken fcm 토큰 값
     */
    @Transactional
    public void registerFcmToken(String fcmToken) {
        String kakaoId = authService.getAuthenticatedKakaoId();
        
        // 유저 entity
        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId))
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 토큰 등록 시간과 만료 시간 설정
        LocalDateTime registeredAt = DateTimeUtil.truncateToMinute(LocalDateTime.now());
        LocalDateTime expiresAt = registeredAt.plusDays(30);

        // FCM 토큰 entity 빌드
        Fcm fcm = Fcm.builder()
                .token(fcmToken)
                .registeredAt(registeredAt)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        // FCM 토큰 저장
        fcmRepository.save(fcm);
    }

    /**
     * 로그아웃 시, 해당 기기에 대한 사용자의 fcm 토큰 정보 삭제
     *
     * @param fcmToken fcm 토큰 값
     */
    @Transactional
    public void deleteFcmToken(String fcmToken) {
        fcmRepository.deleteByToken(fcmToken);
    }

    /**
     * 로그인 세션이 유지된 상태에서 접속 시, fcm 토큰 만료 기간 갱신
     *
     * @param fcmToken fcm 토큰 값
     */
    @Transactional
    public void renewFcmToken(String fcmToken) {
        // 토큰 업데이트 시간과 만료 시간 설정
        LocalDateTime updatedAt = DateTimeUtil.truncateToMinute(LocalDateTime.now());
        LocalDateTime expiresAt = updatedAt.plusDays(30);

        // 토큰값이 같은 record 업데이트
        fcmRepository.updateToken(fcmToken, updatedAt, expiresAt);
    }

    /**
     * 매일 자정에 만료된 fcm 토큰 삭제 (스케줄링)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void expireFcmToken() {
        // 현재 시간 기준으로 만료 시간이 지난 토큰 record 삭제
        LocalDateTime currentDate = LocalDateTime.now();
        fcmRepository.deleteByExpiresAtBefore(currentDate);
    }
}
