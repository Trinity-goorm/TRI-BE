package com.trinity.ctc.domain.seat.entity;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.util.validator.CapacityValidator;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
public class SeatAvailability {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_availability_seq_generator")
    @SequenceGenerator(
            name = "seat_availability_seq_generator",
            sequenceName = "seat_availability_seq",
            allocationSize = 1000
    )
    private long id;

    private LocalDate reservationDate;
    private int availableSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id")
    private SeatType seatType;

    /* 내부 로직 */
    public void preoccupyOneSeat() {
        CapacityValidator.validateAvailableSeats(this.availableSeats);
        availableSeats--;
    }

    public void cancelOneReservation() {
        CapacityValidator.validateAvailableSeats(this.availableSeats);
        availableSeats++;
    }

    /* 객체 생성 로직 */
    public static SeatAvailability create(Restaurant restaurant, LocalDate ReservationDate, ReservationTime reservationTime, SeatType seatType, int availableSeats) {
        SeatAvailability availability = new SeatAvailability();
        availability.restaurant = restaurant;
        availability.reservationDate = ReservationDate;
        availability.reservationTime = reservationTime;
        availability.seatType = seatType;
        availability.availableSeats = availableSeats;
        return availability;
    }
}
