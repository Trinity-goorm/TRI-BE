package com.trinity.ctc.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ReservationAvailabilityResponse {
    private LocalDate date;
    private boolean available;
}

