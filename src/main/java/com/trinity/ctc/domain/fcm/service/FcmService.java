package com.trinity.ctc.domain.fcm.service;

import com.trinity.ctc.domain.fcm.dto.FcmTokenRequest;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.UserErrorCode;
import com.trinity.ctc.util.formatter.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    public final FcmRepository fcmRepository;
    public final UserRepository userRepository;

    /**
     * 로그인 시, 해당 기기에 대한 사용자의 fcm 토큰 정보 초기화
     */
    public void registerFcmToken(FcmTokenRequest fcmTokenRequest, Long userId) {
        // 유저 entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        LocalDateTime registeredAt = DateTimeUtil.convertMillisToLocalDateTime(fcmTokenRequest.getTimestamp());
        LocalDateTime expiresAt = registeredAt.plusDays(30);
        ;

        Fcm fcm = Fcm.builder()
                .token(fcmTokenRequest.getFcmToken())
                .registeredAt(registeredAt)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        fcmRepository.save(fcm);
    }

    /**
     * 로그아웃 시, 해당 기기에 대한 사용자의 fcm 토큰 정보 삭제
     */
    public void deleteFcmToken(FcmTokenRequest fcmTokenRequest) {
        fcmRepository.deleteByToken(fcmTokenRequest.getFcmToken());
    }

    /**
     * 로그인 세션이 유지된 상태에서 접속 시, fcm 토큰 만료 기간 갱신
     */
    public void renewFcmToken(FcmTokenRequest fcmTokenRequest) {
        String fcmToken = fcmTokenRequest.getFcmToken();
        LocalDateTime updatedAt = DateTimeUtil.convertMillisToLocalDateTime(fcmTokenRequest.getTimestamp());
        LocalDateTime expiresAt = updatedAt.plusDays(30);
        ;

        fcmRepository.updateToken(fcmToken, updatedAt, expiresAt);
    }

    /**
     * 매일 자정에 만료된 fcm 토큰 삭제 (스케줄링)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void expireFcmToken() {
        Date currentDate = new Date();
        fcmRepository.deleteByExpiresAtBefore(currentDate);
    }
}
