package com.trinity.ctc.domain.notification.service;

import com.google.common.collect.Lists;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.notification.dto.SubscriptionListResponse;
import com.trinity.ctc.domain.notification.dto.SubscriptionResponse;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.notification.repository.SeatNotificationRepository;
import com.trinity.ctc.domain.notification.repository.SeatNotificationSubscriptionRepository;
import com.trinity.ctc.domain.notification.sender.NotificationSender;
import com.trinity.ctc.domain.notification.type.NotificationType;
import com.trinity.ctc.domain.notification.validator.EmptyTicketValidator;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.repository.SeatRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.NotificationErrorCode;
import com.trinity.ctc.global.exception.error_code.SeatErrorCode;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import com.trinity.ctc.global.kakao.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription.createSeatNotificationSubscription;
import static com.trinity.ctc.domain.notification.formatter.NotificationFormatter.formattingSeatNotification;
import static com.trinity.ctc.domain.notification.formatter.NotificationHistoryFormatter.formattingMulticastNotificationHistory;
import static com.trinity.ctc.domain.notification.formatter.NotificationMessageFormatter.createMulticastMessageWithUrl;
import static com.trinity.ctc.domain.notification.type.NotificationType.SEAT_NOTIFICATION;

@Slf4j
@EnableAsync
@Service
@RequiredArgsConstructor
public class SeatNotificationService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final SeatNotificationRepository seatNotificationRepository;
    private final SeatNotificationSubscriptionRepository seatNotificationSubscriptionRepository;
    private final FcmRepository fcmRepository;

    private final NotificationHistoryService notificationHistoryService;
    private final AuthService authService;
    private final NotificationSender notificationSender;

    // 발송할 알림의 batch-size(Firebase Messaging service 에서 send 메서드의 요청으로 보낼 수 있는 최대 건수)
    private final int BATCH_SIZE = 500;

    /**
     * 빈자리 알림 구독 메서드
     * @param seatId 좌석 ID
     */
    @Transactional
    public void subscribeSeatNotification(Long seatId) {
        // 사용자의 kakaoId get
        String kakaoId = authService.getAuthenticatedKakaoId();
        // kakaoId로 사용자 조회, 없을 시 404 반환
        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId)).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 티켓 개수 검증, 509 반환
        EmptyTicketValidator.validateEmptyTicketUsage(user.getEmptyTicketCount());

        // 좌석 정보 조회, 없을 시 404 반환
        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new CustomException(SeatErrorCode.NOT_FOUND));

        // 해당 자리에 대한 빈알림 데이터 조회
        SeatNotification seatNotification = seatNotificationRepository.findBySeatId(seatId)
                // 조회 결과가 없을 경우, 빈자리 알림 데이터 생성
                .orElseGet(() -> registerSeatNotificationMessage(seat));

        // 이미 신청 내역이 있을 시, 409 반환
        seatNotificationSubscriptionRepository.findByUserIdAndSeatNotification(user.getId(), seatNotification)
                .ifPresent(notification -> {
                    throw new CustomException(NotificationErrorCode.ALREADY_SUBSCRIBED);
                });

        // 사용자가 해당 좌석에 대해 예약 완료 또는 예약 중인 예약 건이 있는지 여부 조회
        List<ReservationStatus> statusList = Arrays.asList(ReservationStatus.COMPLETED, ReservationStatus.IN_PROGRESS);
        boolean isReserved = reservationRepository.existsByReservationDataV1(user.getId(),
                seat.getRestaurant().getId(),
                seat.getReservationDate(),
                seat.getReservationTime().getTimeSlot(),
                seat.getSeatType().getId(),
                statusList);
        // 해당 자리에 대한 예약 건이 있을 경우, 422 반환
        if (isReserved) throw new CustomException(NotificationErrorCode.ALREADY_RESERVED);

        // 빈자리 알림 구독 entity 생성(factory 메서드 호출)
        SeatNotificationSubscription seatNotificationSubscription = createSeatNotificationSubscription(user, seatNotification);

        // 빈자리 알림 신청 시, 빈자리 알림 티켓 -1
        user.useEmptyTicket();

        // 빈자리 알림 신청 내역 저장
        seatNotificationSubscriptionRepository.save(seatNotificationSubscription);
    }

    /**
     * 빈자리 알림에 대해 최초 구독 신청 시, 빈자리 알림을 생성하여 저장하는 내부 메서드
     * @param seat 좌석 entity
     * @return 빈자리 알림 entity
     */
    private SeatNotification registerSeatNotificationMessage(Seat seat) {
        // 빈자리 알림 생성
        SeatNotification seatNotification = formattingSeatNotification(seat);
        // 빈자리 알림 저장 후 해당 entity 반환
        return seatNotificationRepository.save(seatNotification);
    }

    /**
     * 빈자리 알림 구독 취소 메서드
     * @param seatNotificationId 빈자리 알림 구독 ID
     */
    @Transactional
    public void cancelSubscribeSeatNotification(Long seatNotificationId) {
        // 해당 빈자리 알림 구독 data 삭제
        seatNotificationSubscriptionRepository.deleteById(seatNotificationId);
    }

    /**
     * 사용자의 빈자리 알림 신청 내역 반환 메서드
     * @return SubscriptionListResponse(빈자리 알림 관련 정보 responseDTO)
     */
    @Transactional(readOnly = true)
    public SubscriptionListResponse getSeatNotifications() {
        // 사용자의 kakaoId get
        String kakaoId = authService.getAuthenticatedKakaoId();
        // kakaoId로 사용자 조회, 없을 시 404 반환
        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId)).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 사용자의 모든 빈자리 알림 구독 정보 조회
        List<SeatNotificationSubscription> seatNotificationSubscriptionList = seatNotificationSubscriptionRepository.findAllByUserId(user.getId());
        // 각 빈자리 알림에 대한 responseDTO를 저장할 list 초기화
        List<SubscriptionResponse> subscriptionResponseList = new ArrayList<>();

        // 사용자가 구독한 모든 빈자리 알림에 대한 정보로 ResponseDTO 생성
        for (SeatNotificationSubscription notificationSubscription : seatNotificationSubscriptionList) {
            // 해당 빈자리 알림을 구독한 사용자 수 조회
            int subscriberCount = seatNotificationSubscriptionRepository.countBySeatNotification(notificationSubscription.getSeatNotification());

            // 빈자리 알림 ID와 해당 좌석 entity, 구독자 수로 ResponseDTO를 생성하는 factory 메서드 호출
            SubscriptionResponse subscriptionResponse = SubscriptionResponse.of(
                    notificationSubscription.getSeatNotification().getId(),
                    notificationSubscription.getSeatNotification().getSeat(),
                    subscriberCount
            );
            // list에 추가
            subscriptionResponseList.add(subscriptionResponse);
        }

        // 각 빈자리 알림에 대한 ResponseDTO 들과 총 신청 개수를 담은 ListResponse 생성/반환
        return new SubscriptionListResponse(subscriptionResponseList.size(), subscriptionResponseList);
    }

    /**
     * 빈자리에 대한 예약 취소 이벤트 발생 시, 빈자리 알림 발송 로직을 수행하는 메서드
     * @param seatId 좌석 ID
     */
    @Async("empty-seat-notification")
    @Transactional(readOnly = true)
    public void sendSeatNotification(long seatId) {
        log.info("✅✅✅ 빈자리 알림 발송 시작!");
        long startTime = System.nanoTime(); // 시작 시간 측정

        // 전송 메서드의 반환값인 알림 history List 를 저장할 list 초기화
        List<CompletableFuture<List<NotificationHistory>>> resultList = new ArrayList<>();
        // 빈자리 알림 정보 조회, 없을 시 404 반환
        SeatNotification seatNotification = seatNotificationRepository.findBySeatId(seatId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOT_FOUND));
        // 빈자리 알림 구독자 리스트 조회 -> user와 fcm을 fetch join
        List<SeatNotificationSubscription> seatNotificationSubscriptionList = seatNotificationSubscriptionRepository.findAllBySeatNotification(seatNotification);
        // 없을 시 404 반환
        if (seatNotificationSubscriptionList.isEmpty()) throw new CustomException(NotificationErrorCode.NO_SUBSCRIPTION);
        // 빈자리 알림 구독 리스트에서 userList를 get
        List<User> userList = seatNotificationSubscriptionList.stream().map(SeatNotificationSubscription::getUser).toList();
        // userList에서 각 user의 Fcm을 get -> list로 변환
        List<Fcm> fcmList = userList.stream().map(User::getFcmList).flatMap(List::stream).toList();
        // batch-size(500)으로 발송 대상 FCM 리스트 파티셔닝
        List<List<Fcm>> batches = Lists.partition(fcmList, BATCH_SIZE);

        int batchCount = batches.size(); // 배치 작업 전체 수
        int sendingCount = 0; // 발송된 배치 수 측정

        // 파티셔닝된 배치 별로 발송 로직 수행
        for (List<Fcm> batch : batches) {
            log.info("✅ 알림 발송 시작 Batch {}", sendingCount);

            // 알림 발송 메서드 호출 -> 반환값 resultList에 추가(반환 타입: CompletableFuture<List<NotificationHistory>>)
            resultList.add(sendNotification(seatNotification, batch, SEAT_NOTIFICATION));

            sendingCount++;  // 발송된 배치 수 ++
            log.info("✅ 알림 발송 완료 Batch {}", sendingCount);
            if (sendingCount == batchCount) log.info("✅✅ 전송완료!!!!!!!!!!!!!"); // if(발송 수 == 총 배치 작업 수)
        }

        long endTime = System.nanoTime();  // 종료 시간 측정
        long elapsedTimeTosend = endTime - startTime;  // 발송까지의 경과 시간
        log.info("✅✅✅ 당일 예약 알림 발송 실행 시간: {} ms", elapsedTimeTosend / 1_000_000);

        // 전송 결과로 반환된 각 future 의 전달이 완료될 때까지 기다린 후 알림 history 리스트로 반환
        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream) // 여러 리스트를 하나로 합침
                .toList();

        long elapsedTimeToResponse = endTime - startTime; // 응답 반환까지의 경과 시간
        log.info("✅✅✅ 당일 예약 알림 응답 시간: {} ms", elapsedTimeToResponse / 1_000_000);

        // 전송된 알림의 히스토리를 전부 history 테이블에 저장하는 메서드 호출
        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 발송할 MulticastMessage 객체를 생성하고 발송하는 메서드
     * @param seatNotification 빈자리 알림 entity
     * @param batch batch-size 별로 나눈 Fcm list
     * @return 알림 history List를 비동기로 반환하는 CompletableFuture 객체 리스트
     */
    @Transactional(readOnly = true)
    public CompletableFuture<List<NotificationHistory>> sendNotification(SeatNotification seatNotification, List<Fcm> batch, NotificationType type) {
        // 발송할 MulticasyMessage 정보를 담은 Wrapper 객체 생성
        FcmMulticastMessage multicastMessage = createMulticastMessageWithUrl(
                seatNotification.getTitle(), seatNotification.getBody(), seatNotification.getUrl(), batch);
        // MulticastMessage 발송 메서드 호출
        return notificationSender.sendMulticastNotification(multicastMessage)
                // 비동기로 알림 정보와 발송 결과, 알림 타입에 맞춰 알림 history List 를 생성하는 메서드 호출 -> 발송 응답 수신 시, List<NotificationHistory>를 반환
                .thenApplyAsync(resultList -> formattingMulticastNotificationHistory(multicastMessage, resultList, type));
    }

    /**
     * 날짜/시간이 지난 자리에 대한 빈자리 알림 메세지/빈자리 알림 신청에 대한 데이터 삭제 메서드
     * @param currentDate 현재 날짜
     * @param currentTime 현재 시간
     */
    @Transactional
    public void deleteSeatNotifications(LocalDate currentDate, LocalTime currentTime) {
        // 현재 날짜/시간 기준으로 이전의 자리에 해당하는 빈자리 알림 메세지를 select
        List<SeatNotification> seatNotificationList = seatNotificationRepository.findAllByCurrentDateTime(currentDate, currentTime);
        // 삭제(Cascade 설정으로 알림 신청 데이터도 삭제됨)
        seatNotificationRepository.deleteAll(seatNotificationList);
    }
}
