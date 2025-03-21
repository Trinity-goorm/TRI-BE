package com.trinity.ctc.domain.user.dto;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 예약 리스트 응답")
public class UserReservationListResponse {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;

    @Schema(description = "예약 목록")
    private final List<UserReservationResponse> reservations;

    public static UserReservationListResponse from(Slice<Reservation> reservations) {
        List<UserReservationResponse> responseList = reservations.getContent().stream()
                .map(UserReservationResponse::from)
                .toList();

        return new UserReservationListResponse(reservations.hasNext(), responseList);
    }
}
