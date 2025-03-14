package com.trinity.ctc.domain.seat.service;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.service.provider.SeatDataProvider;
import com.trinity.ctc.global.records.DateRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatPreparationServiceTest {

    @Mock
    private SeatDataProvider seatDataProvider;

    @InjectMocks
    private SeatPreparationService seatPreparationService;

    private List<Restaurant> mockRestaurants;
    private List<ReservationTime> mockReservationTimes;
    private List<SeatType> mockSeatTypes;
    private DateRange mockDateRange;

    /**
     * 테스트 데이터 초기화 (`@BeforeEach`)
     * - 2개 식당 (`Restaurant A`, `Restaurant B`)
     * - 3개 좌석 타입 (`2인석`, `4인석`, `6인석`)
     * - 2개 예약 시간 (`09:00`, `10:00`)
     * - 날짜 범위: `2025-04-01` ~ `2025-05-01` (총 30일)
     */
    @BeforeEach
    void setUp() {
        mockRestaurants = List.of(
                new Restaurant("Restaurant A", "Address A", "010-1234-5678", "WiFi", "09:00-22:00", "Weekends", "1hr", "No pets", false, 100, 4.5, 20000, null, null),
                new Restaurant("Restaurant B", "Address B", "010-8765-4321", "Parking", "10:00-21:00", "Weekdays", "1.5hr", "No smoking", false, 80, 4.2, 25000, null, null)
        );

        mockSeatTypes = List.of(
                new SeatType(1, 2),  // 2인석
                new SeatType(3, 4),  // 4인석
                new SeatType(5, 6)  // 6인석
        );

        mockReservationTimes = List.of(
                new ReservationTime(LocalTime.of(9, 0), LocalDateTime.now()),  // 09:00
                new ReservationTime(LocalTime.of(10, 0), LocalDateTime.now())  // 10:00
        );

        mockDateRange = new DateRange(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 1));

        when(seatDataProvider.getAllRestaurants()).thenReturn(mockRestaurants);
        when(seatDataProvider.getAllReservationTimes()).thenReturn(mockReservationTimes);
        when(seatDataProvider.getAllSeatTypes()).thenReturn(mockSeatTypes);
    }

    @Test
    @DisplayName("좌석이 정상적으로 생성되는지 검증")
    void 성공적으로_알맞은_개수의_데이터_생성() {
        int availableSeatCount = 10;

        List<Seat> seats = seatPreparationService.generateSeats(mockDateRange, availableSeatCount);

        assertThat(seats).isNotEmpty();
        assertThat(seats).hasSize(360); // 5일 × 2개 식당 × 2개 시간 × 3개 좌석 타입 = 60개
    }

    /**
     * 데이터가 없을 때 빈 리스트가 반환되는지 확인
     */
    @Test
    @DisplayName("데이터가 없을 때 빈 리스트 반환")
    void 빈_데이터_반환_테스트() {
        when(seatDataProvider.getAllRestaurants()).thenReturn(Collections.emptyList());
        when(seatDataProvider.getAllReservationTimes()).thenReturn(Collections.emptyList());
        when(seatDataProvider.getAllSeatTypes()).thenReturn(Collections.emptyList());

        List<Seat> seats = seatPreparationService.generateSeats(mockDateRange, 10);

        assertThat(seats).isEmpty();
    }

    /**
     * 성능 테스트: `generateSeats()`가 1초 내에 실행되는지 확인
     */
    @Test
    @DisplayName("성능 테스트 - 1초 내 실행 여부 검증")
    void 좌석_생성_성능_테스트() {
        assertTimeout(Duration.ofSeconds(1), () -> {
            seatPreparationService.generateSeats(mockDateRange, 10);
        });
    }

    /**
     * `seatDataProvider`의 각 메서드가 한 번씩 호출되었는지 검증
     */
    @Test
    @DisplayName("seatDataProvider의 각 메서드 호출 검증")
    void seatDataProvider_메서드_호출_검증() {
        seatPreparationService.generateSeats(mockDateRange, 10);

        verify(seatDataProvider, times(1)).getAllRestaurants();
        verify(seatDataProvider, times(1)).getAllReservationTimes();
        verify(seatDataProvider, times(1)).getAllSeatTypes();
    }
}
