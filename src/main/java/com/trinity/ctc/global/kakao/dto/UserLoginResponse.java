package com.trinity.ctc.global.kakao.dto;

import com.trinity.ctc.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 로그인 응답")
public class UserLoginResponse {

    @Schema(description = "사용자 식별자", example = "1")
    private Long id;

    @Schema(description = "신규 사용자 여부", example = "true")
    private boolean isNewUser;

    @Schema(description = "사용자 닉네임", example = "trinity")
    private String name;

    @Schema(description = "사용자 전화번호", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "빈 티켓 수", example = "10")
    private int emptyTicketCount;

    @Schema(description = "일반 티켓 수", example = "100")
    private int normalTicketCount;

    @Schema(description = "액세스 토큰", example = "eyJhbGci")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGci")
    private String refreshToken;
    // 신규 회원을 위한 기본값을 설정하는 정적 메서드
    public static UserLoginResponse newUser(User user, KakaoTokenResponse tokenResponse) {
        return new UserLoginResponse(
            user.getId(),
            true,
            null,
            null,
            100,
            10,
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
