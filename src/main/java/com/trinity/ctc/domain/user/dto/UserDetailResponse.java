package com.trinity.ctc.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "사용자 프로필 정보 응답")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDetailResponse {

    @Schema(description = "사용자 ID", example = "1")
    private final long userId;

    @Schema(description = "사용자 이름", example = "김똑똑")
    private final String username;

    @Schema(description = "보유한 일반 티켓 수", example = "100")
    private final int normalTicketCount;

    @Schema(description = "보유한 빈자리알람 티켓 수", example = "10")
    private final int emptyTicketCount;

    public static UserDetailResponse of(long userId, String username, int normalTicketCount, int emptyTicketCount) {
        return new UserDetailResponse(userId, username, normalTicketCount, emptyTicketCount);
    }
}
