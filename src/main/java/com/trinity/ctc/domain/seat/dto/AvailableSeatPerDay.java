package com.trinity.ctc.domain.seat.dto;


import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableSeatPerDay {
    private Long restaurantId;
    private LocalDate reservationDate;
    private Integer availableSeats;
    private LocalTime timeSlot;
}
