package com.trinity.ctc.domain.reservation.service;

import com.trinity.ctc.domain.reservation.dto.PreoccupyResponse;
import com.trinity.ctc.domain.reservation.dto.ReservationRequest;
import com.trinity.ctc.domain.reservation.dto.ReservationResultResponse;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.reservation.repository.ReservationTimeRepository;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.domain.reservation.validator.ReservationValidator;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.repository.RestaurantRepository;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.repository.SeatRepository;
import com.trinity.ctc.domain.seat.repository.SeatTypeRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.event.PreOccupancyCanceledEvent;
import com.trinity.ctc.event.ReservationCanceledEvent;
import com.trinity.ctc.event.ReservationCompleteEvent;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.*;
import com.trinity.ctc.util.validator.SeatAvailabilityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    //  이벤트 발행하는 인터페이스
    private final ApplicationEventPublisher eventPublisher;
    private final ReservationValidator reservationValidator;

    /**
     * 선점하기
     *
     * @param reservationRequest
     * @return 선점성공여부
     */
    @Transactional
    public PreoccupyResponse occupyInAdvance(ReservationRequest reservationRequest) {
        log.info("[예약 요청] 날짜: {}, 시간: {}, 레스토랑 ID: {}, 사용자 ID: {}, 좌석 타입 ID: {}",
                reservationRequest.getSelectedDate(), reservationRequest.getReservationTime(),
                reservationRequest.getRestaurantId(), reservationRequest.getUserId(), reservationRequest.getSeatTypeId());

        // 사용자 예약이력 검증
        reservationValidator.validateUserReservation(reservationRequest);

        // 검증 (좌석 남은 자리 확인)
        Seat seat = validateSeatAvailability(reservationRequest);

        // 좌석 한개 선점
        log.info("[좌석 선점] 기존 좌석 수: {}", seat.getAvailableSeats());
        seat.preoccupyOneSeat();
        log.info("[좌석 선점 완료] 남은 좌석 수: {}", seat.getAvailableSeats());

        // 예약정보 생성 -> 저장
        Reservation reservation = createReservation(reservationRequest);
        reservationRepository.save(reservation);

        log.info("[예약 성공] 예약 ID: {}", reservation.getId());

        // DTO 생성
        return PreoccupyResponse.of(true, reservation.getId());
    }

    @Transactional
    public ReservationResultResponse complete(long reservationId, long userId) {

        log.info("[예약 정보] 예약정보 ID: {}, 예약자 ID: {}", reservationId, userId);

        // 예약정보 가져오기
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        // 예약정보의 사용자 검증
        ReservationValidator.validateReservationUserMatched(reservation.getUser().getId(), userId);

        // 예약정보 선점여부 검증
        ReservationValidator.isPreoccupied(reservation.getStatus());

        // 티켓 차감
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        user.payNormalTickets();

        // 예약정보 완료상태로 변경
        reservation.completeReservation();

        // 예약 완료 이벤트 발행
        eventPublisher.publishEvent(new ReservationCompleteEvent(reservation.getUser().getId(), reservation.getId()));

        // 결과 반환
        return ReservationResultResponse.of(true, reservationId, reservation.getRestaurant().getName(), reservation.getReservationDate(), reservation.getReservationTime().getTimeSlot());
    }

    @Transactional
    public ReservationResultResponse cancelPreoccupy(long reservationId, long userId) {
        log.info("[예약 정보] 예약정보 ID: {}, 예약자 ID: {}", reservationId, userId);

        // 예약정보 가져오기
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        // 예약정보 선점여부 검증
        ReservationValidator.isPreoccupied(reservation.getStatus());

        // 예약정보 취소 상태로 변경
        reservation.cancelReservation();

        // 가용좌석 증가 (더티체킹)
        Seat seat = seatRepository.findByReservationData(reservation.getRestaurant().getId(), reservation.getReservationDate(), reservation.getReservationTime().getTimeSlot(), reservation.getSeatType().getId());
        log.info("[예약 취소 전] 가용좌석 수: {}", seat.getAvailableSeats());
        seat.cancelOneReservation();
        log.info("[예약 취소 후] 가용좌석 수: {}", seat.getAvailableSeats());

        eventPublisher.publishEvent(new PreOccupancyCanceledEvent(reservationId, seat.getId(), seat.getAvailableSeats()));
        // 결과 반환
        return ReservationResultResponse.of(true, reservationId, reservation.getRestaurant().getName(), reservation.getReservationDate(), reservation.getReservationTime().getTimeSlot());
    }

    @Transactional
    public ReservationResultResponse cancelReservation(long reservationId) {
        log.info("[예약 정보] 예약정보 ID: {}", reservationId);

        // 예약정보 가져오기
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        // 예약정보 완료여부 검증
        ReservationValidator.isCompleted(reservation.getStatus());

        // 예약정보 상태 변경
        reservation.cancelReservation();

        // 예약시점 검증 (날짜기준 이틀 전인가?)
        boolean isCODPassed = ReservationValidator.checkCOD(reservation.getReservationDate());

        // 티켓 반환 로직
        User user = reservation.getUser();
        log.info("[티켓 반환 전] 반환여부: {}, 일반티켓 수: {}", isCODPassed, user.getNormalTicketCount());
        if (isCODPassed) {
            user.returnNormalTickets();
        }
        log.info("[티켓 반환 후] 반환여부: {}, 일반티켓 수: {}", isCODPassed, user.getNormalTicketCount());

        // 좌석 수 반환
        Seat seat = seatRepository.findByReservationData(reservation.getRestaurant().getId(), reservation.getReservationDate(), reservation.getReservationTime().getTimeSlot(), reservation.getSeatType().getId());

        log.info("id:" + seat.getId());

        log.info("[예약 취소 전] 가용좌석 수: {}", seat.getAvailableSeats());
        seat.cancelOneReservation();
        log.info("[예약 취소 후] 가용좌석 수: {}", seat.getAvailableSeats());

        eventPublisher.publishEvent(new ReservationCanceledEvent(user.getId(), reservationId, seat.getId(), seat.getAvailableSeats(), isCODPassed));

        return ReservationResultResponse.of(true, reservationId, reservation.getRestaurant().getName(), reservation.getReservationDate(), reservation.getReservationTime().getTimeSlot(), isCODPassed);
    }

    /**
     * 선점가능상태 검증
     *
     * @param reservationRequest
     * @return 선점대상 좌석정보
     */
    private Seat validateSeatAvailability(ReservationRequest reservationRequest) {
        Seat seat = seatRepository.findByReservationData(
                reservationRequest.getRestaurantId(),
                reservationRequest.getSelectedDate(),
                reservationRequest.getReservationTime(),
                reservationRequest.getSeatTypeId()
        );

        if (!SeatAvailabilityValidator.checkAvailability(seat)) {
            log.warn("[예약 실패] 좌석 부족 - 레스토랑 ID: {}, 날짜: {}, 시간: {}, 좌석 타입 ID: {}",
                    reservationRequest.getRestaurantId(), reservationRequest.getSelectedDate(),
                    reservationRequest.getReservationTime(), reservationRequest.getSeatTypeId());
            throw new CustomException(SeatErrorCode.NO_AVAILABLE_SEAT);
        }

        log.info("[좌석 확인 완료] 남은 좌석 수: {}", seat.getAvailableSeats());
        return seat;
    }

    /**
     * 예약정보 생성
     *
     * @param reservationRequest
     * @return 예약정보
     */
    private Reservation createReservation(ReservationRequest reservationRequest) {
        User user = userRepository.findById(reservationRequest.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(reservationRequest.getRestaurantId())
                .orElseThrow(() -> new CustomException(RestaurantErrorCode.NOT_FOUND));

        SeatType seatType = seatTypeRepository.findById(reservationRequest.getSeatTypeId())
                .orElseThrow(() -> new CustomException(SeatTypeErrorCode.NOT_FOUND));

        ReservationTime reservationTime = reservationTimeRepository.findByTimeSlot(reservationRequest.getReservationTime())
                .orElseThrow(() -> new CustomException(ReservationTimeErrorCode.NOT_FOUND));

        return Reservation.builder()
                .reservationDate(reservationRequest.getSelectedDate())
                .status(ReservationStatus.IN_PROGRESS)
                .restaurant(restaurant)
                .user(user)
                .reservationTime(reservationTime)
                .seatType(seatType)
                .build();
    }
}
