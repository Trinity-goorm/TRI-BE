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
import com.trinity.ctc.domain.reservation.event.PreOccupancyCanceledEvent;
import com.trinity.ctc.domain.reservation.event.ReservationCanceledEvent;
import com.trinity.ctc.domain.reservation.event.ReservationCompleteEvent;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.*;
import com.trinity.ctc.global.kakao.service.AuthService;
import com.trinity.ctc.global.util.validator.SeatAvailabilityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

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
    private final AuthService authService;

    // 레디스
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 선점하기
     * @return 선점성공여부
     */
    @Transactional
    public PreoccupyResponse occupyInAdvance(Long kakaoId, Long restaurantId, LocalDate reservationDate,
                                             LocalTime reservationTime, Long seatTypeId) {

        // 사용자 예약이력 검증
        reservationValidator.validateUserReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);

        // 검증 (좌석 남은 자리 확인)
        Seat seat = validateSeatAvailability(restaurantId, reservationDate, reservationTime, seatTypeId);

        // 좌석 한개 선점
        log.info("[좌석 선점] 기존 좌석 수: {}", seat.getAvailableSeats());
        seat.preoccupyOneSeat();
        log.info("[좌석 선점 완료] 남은 좌석 수: {}", seat.getAvailableSeats());

        // 예약정보 생성 -> 저장
        Reservation reservation = createReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);
        reservationRepository.save(reservation);

        log.info("[예약 성공] 예약 ID: {}", reservation.getId());

        // DTO 생성
        return PreoccupyResponse.of(true, reservation.getId());
    }

    /**
     * 선점하기
     * @return 선점성공여부
     */
    @Transactional
    public PreoccupyResponse occupyInAdvanceTest(Long kakaoId, Long restaurantId, LocalDate reservationDate,
                                                 LocalTime reservationTime, Long seatTypeId) {

        // 사용자 예약이력 검증
        reservationValidator.validateUserReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);

        // 검증 (좌석 남은 자리 확인)
        Seat seat = validateSeatAvailability(restaurantId, reservationDate, reservationTime, seatTypeId);

        // 좌석 한개 선점
        log.info("[좌석 선점] 기존 좌석 수: {}", seat.getAvailableSeats());
        seat.preoccupyOneSeat();
        log.info("[좌석 선점 완료] 남은 좌석 수: {}", seat.getAvailableSeats());

        // 예약정보 생성 -> 저장
        Reservation reservation = createReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);
        reservationRepository.save(reservation);

        log.info("[예약 성공] 예약 ID: {}", reservation.getId());

        // DTO 생성
        return PreoccupyResponse.of(true, reservation.getId());
    }

    /**
     * 레디스 분산락을 사용한 예약선점기능
     * @param kakaoId
     * @param restaurantId
     * @param reservationDate
     * @param reservationTime
     * @param seatTypeId
     * @return
     */
    @Transactional
    public PreoccupyResponse occupyWithRedisLock(Long kakaoId, Long restaurantId, LocalDate reservationDate,
                                                 LocalTime reservationTime, Long seatTypeId) {

        String lockKey = "lock:reservation:" + restaurantId + ":" + reservationDate + ":" + reservationTime + ":" + seatTypeId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 시도 (최대 5초 대기, 3초 후 자동 해제)
            if (!lock.tryLock(5, 3, TimeUnit.SECONDS)) {
                throw new CustomException(ReservationErrorCode.PREOCCUPY_FAILED);
            }

            // 사용자 예약이력 검증
            reservationValidator.validateUserReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);

            // 좌석 확인
            Seat seat = seatRepository.findByReservationData(restaurantId, reservationDate, reservationTime, seatTypeId);

            // 좌석 1개 선점
            seat.preoccupyOneSeat();
            seatRepository.save(seat);

            // 예약 정보 저장
            Reservation reservation = createReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);
            reservationRepository.save(reservation);

            return PreoccupyResponse.of(true, reservation.getId());

        } catch (InterruptedException e) {
            throw new RuntimeException("락 획득 실패", e);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    /**
     * DB의 원자적 연산을 사용한 예약선점 기능
     * @param kakaoId
     * @param restaurantId
     * @param reservationDate
     * @param reservationTime
     * @param seatTypeId
     * @return
     */
    @Transactional
    public PreoccupyResponse occupyWithAtomicUpdate(Long kakaoId, Long restaurantId, LocalDate reservationDate,
                                                    LocalTime reservationTime, Long seatTypeId) {

        // 사용자 예약이력 검증
        reservationValidator.validateUserReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);

        // 좌석 선점 (원자적 처리)
        int updated = seatRepository.preoccupySeat(restaurantId, reservationDate, reservationTime, seatTypeId);
        if (updated == 0) {
            // 선점 실패 (좌석 부족)
            throw new CustomException(SeatErrorCode.NO_AVAILABLE_SEAT);
        }

        // 예약 정보 저장
        Reservation reservation = createReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);
        reservationRepository.save(reservation);

        log.info("[예약 성공] 예약 ID: {}", reservation.getId());

        return PreoccupyResponse.of(true, reservation.getId());
    }

    /**
     * 레디스 원자적 연산 적용 예약선점
     * @param kakaoId
     * @param restaurantId
     * @param reservationDate
     * @param reservationTime
     * @param seatTypeId
     * @return
     */
    @Transactional
    public PreoccupyResponse occupyWithRedisAtomic(Long kakaoId, Long restaurantId, LocalDate reservationDate,
                                                   LocalTime reservationTime, Long seatTypeId) {

        // 사용자 예약 이력 검증
        reservationValidator.validateUserReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);

        String redisKey = generateRedisKey(restaurantId, reservationDate, reservationTime, seatTypeId);
        String seatCount = stringRedisTemplate.opsForValue().get(redisKey);

        if (seatCount == null) {
            // 1. 최초 1명만 DB 조회 후 Redis에 저장하도록 setIfAbsent로 원자성 보장
            Seat seat = seatRepository.findByReservationData(restaurantId, reservationDate, reservationTime, seatTypeId);
            if (seat == null) throw new CustomException(SeatErrorCode.NOT_FOUND);

            Boolean success = stringRedisTemplate.opsForValue()
                    .setIfAbsent(redisKey, String.valueOf(seat.getAvailableSeats()), Duration.ofMinutes(10));

            if (Boolean.TRUE.equals(success)) {
                log.info("[Redis 초기화 성공] {} 좌석 수: {}", redisKey, seat.getAvailableSeats());
            } else {
                log.info("[Redis 초기화 경쟁 발생, 이미 초기화됨] {}", redisKey);
            }
        }

        // 2. Redis 원자적 감소
        Long remain = stringRedisTemplate.opsForValue().decrement(redisKey);
        if (remain == null || remain < 0) {
            // 좌석 부족 시 롤백
            stringRedisTemplate.opsForValue().increment(redisKey); // 감소했던거 복구
            throw new CustomException(SeatErrorCode.NO_AVAILABLE_SEAT);
        }

        // 3. 감소된 Redis 값을 그대로 DB에 덮어쓰기 (여기서 -1 연산 안 함!!)
        int updated = seatRepository.syncSeatWithRedis(remain, restaurantId, reservationDate, reservationTime, seatTypeId);
        if (updated == 0) {
            // 비정상 상황 → 롤백
            throw new CustomException(SeatErrorCode.REDIS_SYNC_FAILED);
        }

        // 4. 예약 정보 DB 저장
        Reservation reservation = createReservation(kakaoId, restaurantId, reservationDate, reservationTime, seatTypeId);
        reservationRepository.save(reservation);

        return PreoccupyResponse.of(true, reservation.getId());
    }


    @Transactional
    public ReservationResultResponse complete(long reservationId) {

        log.info("[예약 정보] 예약정보 ID: {}", reservationId);

        // 사용자 정보 획득
        Long kakaoId = Long.parseLong(authService.getAuthenticatedKakaoId());

        // 예약정보 가져오기
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        // 예약정보의 사용자 검증
        ReservationValidator.validateReservationUserMatched(reservation.getUser().getKakaoId(), kakaoId);

        // 예약정보 선점여부 검증
        ReservationValidator.isPreoccupied(reservation.getStatus());

        // 티켓 차감
        User user = userRepository.findByKakaoId(kakaoId)
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
    public ReservationResultResponse cancelPreoccupy(long reservationId) {
        log.info("[예약 정보] 예약정보 ID: {}", reservationId);

        // 예약정보 가져오기
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        // 예약정보 선점여부 검증
        ReservationValidator.isPreoccupied(reservation.getStatus());

        // 예약정보 취소 상태로 변경
        reservation.failReservation();

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
     * @return 선점대상 좌석정보
     */
    private Seat validateSeatAvailability(Long restaurantId, LocalDate reservationDate,
                                          LocalTime reservationTime, Long seatTypeId) {
        Seat seat = seatRepository.findByReservationData(
                restaurantId,
                reservationDate,
                reservationTime,
                seatTypeId
        );

        if (!SeatAvailabilityValidator.checkAvailability(seat)) {
            log.warn("[예약 실패] 좌석 부족 - 레스토랑 ID: {}, 날짜: {}, 시간: {}, 좌석 타입 ID: {}",
                    restaurantId,
                    reservationDate,
                    reservationTime,
                    seatTypeId);
            throw new CustomException(SeatErrorCode.NO_AVAILABLE_SEAT);
        }

        log.info("[좌석 확인 완료] 남은 좌석 수: {}", seat.getAvailableSeats());
        return seat;
    }

    /**
     * 예약정보 생성
     * @return 예약정보
     */
    private Reservation createReservation(Long kakaoId, Long restaurantId, LocalDate reservationDate,
                                          LocalTime reservationTimeSlot, Long seatTypeId) {
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RestaurantErrorCode.NOT_FOUND));

        SeatType seatType = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new CustomException(SeatTypeErrorCode.NOT_FOUND));

        ReservationTime reservationTime = reservationTimeRepository.findByTimeSlot(reservationTimeSlot)
                .orElseThrow(() -> new CustomException(ReservationTimeErrorCode.NOT_FOUND));

        return Reservation.builder()
                .reservationDate(reservationDate)
                .status(ReservationStatus.IN_PROGRESS)
                .restaurant(restaurant)
                .user(user)
                .reservationTime(reservationTime)
                .seatType(seatType)
                .build();
    }

    /**
     * seat 레디스키 생성
     * @param restaurantId
     * @param date
     * @param time
     * @param seatTypeId
     * @return
     */
    private String generateRedisKey(Long restaurantId, LocalDate date, LocalTime time, Long seatTypeId) {
        return String.format("seat:%d:%s:%s:%d", restaurantId, date, time, seatTypeId);
    }
}
