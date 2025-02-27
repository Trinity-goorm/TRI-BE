package com.trinity.ctc.domain.fcm.service;

import com.trinity.ctc.domain.fcm.dto.FcmTokenRequest;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
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

    /**
     * 로그인 시, 해당 기기에 대한 사용자의 fcm 토큰 정보 초기화
     *
     * @param fcmTokenRequest FCM토큰 정보 요청 DTO(토큰값, 등록 시간)
     * @param userId          사용자 ID
     */
    public void registerFcmToken(FcmTokenRequest fcmTokenRequest, Long userId) {
        // 유저 entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 토큰 등록 시간과 만료 시간 설정
        LocalDateTime registeredAt = fcmTokenRequest.getTimeStamp();
        LocalDateTime expiresAt = registeredAt.plusDays(30);

        // FCM 토큰 entity 빌드
        Fcm fcm = Fcm.builder()
                .token(fcmTokenRequest.getFcmToken())
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
     * @param fcmTokenRequest FCM토큰 정보 요청 DTO(토큰값, null)
     */
    public void deleteFcmToken(FcmTokenRequest fcmTokenRequest) {
        fcmRepository.deleteByToken(fcmTokenRequest.getFcmToken());
    }

    /**
     * 로그인 세션이 유지된 상태에서 접속 시, fcm 토큰 만료 기간 갱신
     *
     * @param fcmTokenRequest FCM토큰 정보 요청 DTO(토큰값, 업데이트 시간)
     */
    public void renewFcmToken(FcmTokenRequest fcmTokenRequest) {
        String fcmToken = fcmTokenRequest.getFcmToken();

        // 토큰 업데이트 시간과 만료 시간 설정
        LocalDateTime updatedAt = fcmTokenRequest.getTimeStamp();
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
