package com.trinity.ctc.domain.reservation.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationAvailabilityResponse {

    private LocalDate date;
    private boolean available;
    private Long restaurantId;
}

