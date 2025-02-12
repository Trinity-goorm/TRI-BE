package com.trinity.ctc.kakao.dto;

import com.trinity.ctc.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {
    private Long id;
    private boolean isNewUser;
    private String name;
    private String phoneNumber;
    private int emptyTicketCount;
    private int normalTicketCount;
    private String accessToken;
    private String refreshToken;
    // 신규 회원을 위한 기본값을 설정하는 정적 메서드
    public static UserLoginResponse newUser(User user, KakaoTokenResponse tokenResponse) {
        return new UserLoginResponse(
            user.getId(),
            true,
            null,
            null,
            0,
            0,
            tokenResponse.getAccessToken(),
            tokenResponse.getRefreshToken()
        );
    }

    // 기존 회원을 위한 객체 생성
    public static UserLoginResponse existingUser(User user, KakaoTokenResponse tokenResponse) {
        return new UserLoginResponse(
            user.getId(),
            false,
            user.getNickname(),
            user.getPhoneNumber(),
            user.getEmptyTicketCount(),
            user.getNormalTicketCount(),
            tokenResponse.getAccessToken(),
            tokenResponse.getRefreshToken()
        );
    }
}
