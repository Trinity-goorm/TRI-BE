package com.trinity.ctc.domain.user.dto;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 예약 리스트 응답")
public class UserReservationListResponse {

    @Schema(description = "총 예약 개수", example = "3")
    private final int totalCount;

    @Schema(description = "예약 목록")
    private final List<UserReservationResponse> reservations;

    public static UserReservationListResponse from(List<Reservation> reservations) {
        List<UserReservationResponse> responseList = reservations.stream()
                .map(UserReservationResponse::from)
                .toList();

        return new UserReservationListResponse(responseList.size(), responseList);
    }
}
