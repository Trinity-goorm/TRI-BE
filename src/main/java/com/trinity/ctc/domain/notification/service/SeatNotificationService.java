package com.trinity.ctc.domain.notification.service;

import com.google.common.collect.Lists;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.notification.dto.SubscriptionListResponse;
import com.trinity.ctc.domain.notification.dto.SubscriptionResponse;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.fomatter.NotificationContentUtil;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.notification.repository.SeatNotificationRepository;
import com.trinity.ctc.domain.notification.repository.SeatNotificationSubscriptionRepository;
import com.trinity.ctc.domain.notification.sender.NotificationSender;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.trinity.ctc.domain.notification.entity.SeatNotification.createSeatNotification;
import static com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription.createSeatNotificationSubscription;
import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMulticastMessageWithUrl;
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

    @Transactional
    public void subscribeSeatNotification(Long seatId) {
        String kakaoId = authService.getAuthenticatedKakaoId();
        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId)).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 티켓 개수 검증, 509 반환
        EmptyTicketValidator.validateEmptyTicketUsage(user.getEmptyTicketCount());

        // 해당 자리에 대한 빈알림 데이터 조회, 없을 시 빈자리 알림 데이터 생성
        SeatNotification seatNotification = seatNotificationRepository.findBySeatId(seatId)
                .orElseGet(() -> registerSeatNotificationMessage(seatId));

        // 이미 신청 내역이 있을 시, 409 반환
        seatNotificationSubscriptionRepository.findByUserIdAndSubscription(user.getId(), seatNotification)
                .ifPresent(notification -> {
                    log.info("이미 신청 내역이 존재합니다. userId: {}", user.getId());
                    throw new CustomException(NotificationErrorCode.ALREADY_SUBSCRIBED);
                });

        // 이미 해당 자리에 예약이 되어 있을 경우, 422 반환
        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new CustomException(SeatErrorCode.NOT_FOUND));
        List<ReservationStatus> statusList = Arrays.asList(ReservationStatus.COMPLETED, ReservationStatus.IN_PROGRESS);
        if (reservationRepository.existsByReservationDataV1(user.getId(),
                seat.getRestaurant().getId(),
                seat.getReservationDate(),
                seat.getReservationTime().getTimeSlot(),
                seat.getSeatType().getId(),
                statusList)) throw new CustomException(NotificationErrorCode.ALREADY_RESERVED);

        // 빈자리 알림 신청 내역 build
        SeatNotificationSubscription seatNotificationSubscription = createSeatNotificationSubscription(user, seatNotification);

        // 빈자리 알림 신청 시, 빈자리 알림 티켓 -1
        user.useEmptyTicket();

        // 빈자리 알림 신청 내역 저장
        seatNotificationSubscriptionRepository.save(seatNotificationSubscription);
    }

    /**
     * 빈자리 알림 최초 신청 시, 구독 message를 등록하는 메서드
     *
     * @param seatId
     * @return
     */
    private SeatNotification registerSeatNotificationMessage(long seatId) {
        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new CustomException(SeatErrorCode.NOT_FOUND));

        // 빈자리 알림 메세지에 필요한 정보 변수 선언
        LocalDate date = seat.getReservationDate();
        LocalTime time = seat.getReservationTime().getTimeSlot();
        long restaurantId = seat.getRestaurant().getId();
        String restaurantName = seat.getRestaurant().getName();
        int minCapacity = seat.getSeatType().getMinCapacity();
        int maxCapacity = seat.getSeatType().getMaxCapacity();

        // 알림 메세지 data 별 포멧팅
        String title = NotificationContentUtil.formatSeatNotificationTitle(restaurantName);
        String body = NotificationContentUtil.formatSeatNotificationBody(date, time, minCapacity, maxCapacity);
        String url = NotificationContentUtil.formatSeatNotificationUrl(restaurantId);

        // 알림 메세지 빌드
        SeatNotification seatNotification = createSeatNotification(title, body, url, seat);

        return seatNotificationRepository.save(seatNotification);
    }

    /**
     * 빈자리 알림 신청 취소 메서드
     *
     * @param seatNotificationId
     */
    @Transactional
    public void cancelSubscribeSeatNotification(Long seatNotificationId) {
        seatNotificationSubscriptionRepository.deleteById(seatNotificationId);
    }

    /**
     * 사용자의 빈자리 알림 신청 내역 반환 메서드
     *
     * @return
     */
    @Transactional(readOnly = true)
    public SubscriptionListResponse getSeatNotifications() {
        String kakaoId = authService.getAuthenticatedKakaoId();
        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId)).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        List<SeatNotificationSubscription> seatNotificationSubscriptionList = seatNotificationSubscriptionRepository.findAllByUserId(user.getId());
        List<SubscriptionResponse> subscriptionResponseList = new ArrayList<>();
        log.info("조회된 SeatNotification 개수: {}", seatNotificationSubscriptionList.size());


        for (SeatNotificationSubscription notification : seatNotificationSubscriptionList) {
            int subscriberCount = seatNotificationSubscriptionRepository.countBySeatNotificationMessage(notification.getSeatNotification());

            log.info("SeatNotification ID: {}, 관련 Seat ID: {}, 구독자 수: {}",
                    notification.getId(),
                    notification.getSeatNotification().getSeat().getId(),
                    subscriberCount);

            SubscriptionResponse subscriptionResponse = SubscriptionResponse.of(notification.getId(), notification.getSeatNotification().getSeat(), subscriberCount);
            subscriptionResponseList.add(subscriptionResponse);
            log.info("response: " + subscriptionResponse.getSeatNotificationId());
        }

        return new SubscriptionListResponse(subscriptionResponseList.size(), subscriptionResponseList);
    }

    // 빈자리 알림 발송 시작점

    /**
     * 빈자리에 대한 예약 취소 이벤트 발생 시, 빈자리 알림을 발송하는 메서드
     *
     * @param seatId
     */
    @Async
    @Transactional(readOnly = true)
    public void sendSeatNotification(long seatId) {
        log.info("✅ 빈자리 알림 발송 시작!");
        long startTime = System.nanoTime(); // 시작 시간 측정

        int batchCount = 0;
        int batchSize = 500;

        List<CompletableFuture<List<NotificationHistory>>> resultList = new ArrayList<>();

        // 빈자리 알림 정보
        SeatNotification seatNotification = seatNotificationRepository.findBySeatId(seatId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOT_FOUND));

        // 빈자리 알림 구독자 리스트
        List<SeatNotificationSubscription> seatNotificationSubscriptionList =
                seatNotificationSubscriptionRepository.findAllBySeatNotificationWithUsers(seatNotification).orElseThrow(
                        () -> new CustomException(NotificationErrorCode.NO_SUBSCRIPTION));

        List<User> userList = seatNotificationSubscriptionList.stream().map(SeatNotificationSubscription::getUser).toList();

        List<Fcm> fcmList = fcmRepository.findByUserIn(userList);

        List<List<Fcm>> batches = Lists.partition(fcmList, batchSize);

        int clearCount = batches.size();

        for (List<Fcm> batch : batches) {
            batchCount++;
            CompletableFuture<List<NotificationHistory>> sendingResult = sendSeatNotificationAsync(seatNotification, batch, batchCount, clearCount);
            resultList.add(sendingResult);
        }

        // 모든 배치가 완료된 후 최종 로그를 찍고 시간도 기록
        CompletableFuture.allOf(resultList.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    long endTime = System.nanoTime();  // 종료 시간 측정
                    long elapsedTime = endTime - startTime;  // 경과 시간 (나노초 단위)

                    log.info("빈자리 알림 발송 실행 시간: {} ms", elapsedTime / 1_000_000);
                });

        List<NotificationHistory> notificationHistoryList = resultList.stream()
                .map(CompletableFuture::join) // 각 future의 실행이 끝날 때까지 기다림
                .flatMap(List::stream) // 여러 리스트를 하나로 합침
                .toList();

        notificationHistoryService.saveNotificationHistory(notificationHistoryList);
    }

    @Async("fixedThreadPoolExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<List<NotificationHistory>> sendSeatNotificationAsync(SeatNotification seatNotification, List<Fcm> batch, int batchCount, int clearCount) {

        log.info("✅ 빈자리 알림 발송 (Batch {}): 시작", batchCount);
//        List<String> tokens = batch.stream().map(Fcm::getToken).toList();

        FcmMulticastMessage multicastMessage = createMulticastMessageWithUrl(
                seatNotification.getTitle(), seatNotification.getBody(), seatNotification.getUrl(), batch);

        // FCM 메시지를 비동기 전송 (CompletableFuture 반환)
        return notificationSender.sendMulticastNotification(multicastMessage)
                .thenApplyAsync(resultList -> {
                    log.info("✅ 빈자리 알림 발송 완료 Batch {}", batchCount);
                    if (batchCount == clearCount) log.info("전송완료!!!!!!!!!!!!!");

                    // 전송된 알림 히스토리를 배치로 저장
                    return notificationHistoryService.buildMulticastNotificationHistory(
                            multicastMessage,
                            resultList, SEAT_NOTIFICATION
                    );
                }).exceptionally(e -> {
                    log.error("❌ 빈자리 알림 발송 실패 (Batch {}): {}", batchCount, e.getMessage());
                    return Collections.emptyList(); // 실패 시 빈 리스트 반환
                });
    }

    /**
     * 날짜/시간이 지난 자리에 대한 빈자리 알림 메세지/빈자리 알림 신청에 대한 데이터 삭제 메서드
     */
    @Transactional
    public void deleteSeatNotificationMessages(LocalDate currentDate, LocalTime currentTime) {

        // 현재 날짜/시간 기준으로 이전의 자리에 해당하는 빈자리 알림 메세지를 select
        List<SeatNotification> messages = seatNotificationRepository.findAllByCurrentDateTime(currentDate, currentTime);
        // 삭제(Cascade 설정으로 알림 신청 데이터도 삭제됨)
        seatNotificationRepository.deleteAll(messages);
    }
}
