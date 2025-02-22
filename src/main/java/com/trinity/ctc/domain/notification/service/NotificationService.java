package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.notification.dto.FcmSendingResultDto;
import com.trinity.ctc.domain.notification.dto.SubscriptionListResponse;
import com.trinity.ctc.domain.notification.dto.SubscriptionResponse;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationMessage;
import com.trinity.ctc.domain.notification.repository.NotificationHistoryRepository;
import com.trinity.ctc.domain.notification.repository.ReservationNotificationRepository;
import com.trinity.ctc.domain.notification.repository.SeatNotificationMessageRepository;
import com.trinity.ctc.domain.notification.repository.SeatNotificationRepository;
import com.trinity.ctc.domain.notification.result.SentResult;
import com.trinity.ctc.domain.notification.type.NotificationType;
import com.trinity.ctc.domain.notification.util.fomatter.NotificationMessageUtil;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.reservation.service.ReservationService;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import com.trinity.ctc.domain.seat.repository.SeatAvailabilityRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.event.ReservationCompleteEvent;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.*;
import com.trinity.ctc.util.formatter.DateTimeUtil;
import com.trinity.ctc.util.validator.TicketValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.trinity.ctc.util.formatter.DateTimeUtil.combineWithDate;

@Slf4j
@EnableAsync
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserRepository userRepository;
    private final FcmRepository fcmRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final ReservationNotificationRepository reservationNotificationRepository;
    private final SeatAvailabilityRepository seatAvailabilityRepository;
    private final SeatNotificationMessageRepository seatNotificationMessageRepository;
    private final SeatNotificationRepository seatNotificationRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final ReservationService reservationService;

    /**
     * 예약 이벤트를 통해 예약 알림에 필요한 entity(user, reservation)를 받아오고, 예약 알림 entity을 DB에 저장하는 메서드
     *
     * @param userId        사용자 ID
     * @param reservationId 예약 ID
     */
    @Transactional
    public void registerReservationNotification(Long userId, Long reservationId) {
        // 유저 정보 가져오기
        User user = userRepository.findById(userId).orElseThrow(()
                -> new CustomException(UserErrorCode.NOT_FOUND));

        // 예약 정보 가져오기
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(()
                -> new CustomException(ReservationErrorCode.NOT_FOUND));

        // 당일 예약 알림 메세지 포멧팅
        ReservationNotification dailyNotification = formattingDailyNotification(user, reservation);

        // 예약 1시간 전 알림 메세지 포멧팅
        ReservationNotification hourBeforeNotification = formattingHourBeforeNotification(user, reservation);

        List<ReservationNotification> notificationList = new ArrayList<>(Arrays.asList(dailyNotification, hourBeforeNotification));

        // 알림 2가지 예약 알림 table에 저장
        reservationNotificationRepository.saveAll(notificationList);
    }

    /**
     * registerReservationNotification()의 내부 메서드
     * 당일 예약 알림에 필요한 메세지를 포멧팅하고 예약 알림 entity build
     *
     * @param user        유저 Entity
     * @param reservation 예약 Entity
     * @return
     */
    private ReservationNotification formattingDailyNotification(User user, Reservation reservation) {
        // 당일 알림 메세지에 필요한 정보 변수 선언
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        Long reservationId = reservation.getId();
        LocalDateTime scheduledTime = combineWithDate(reservedDate, LocalTime.of(8, 0));

        // 알림 메세지 data 별 포멧팅
        String title = NotificationMessageUtil.formatDailyNotificationTitle(userName, restaurantName);
        String body = NotificationMessageUtil.formatDailyNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationMessageUtil.formatReservationNotificationUrl(reservationId);

        // 알림 메세지 빌드
        ReservationNotification reservationNotification = ReservationNotification.builder()
                .type(NotificationType.DAILY_NOTIFICATION)
                .title(title)
                .body(body)
                .url(url)
                .user(user)
                .scheduledTime(scheduledTime)
                .reservation(reservation)
                .build();

        return reservationNotification;
    }

    /**
     * registerReservationNotification()의 내부 메서드
     * 1시간 전 예약 알림에 필요한 메세지를 포멧팅하고 예약 알림 entity build
     *
     * @param user        유저 Entity
     * @param reservation 예약 Entity
     * @return
     */
    private ReservationNotification formattingHourBeforeNotification(User user, Reservation reservation) {
        // 당일 알림 메세지에 필요한 정보 변수 선언
        String userName = user.getNickname();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        Long reservationId = reservation.getId();
        LocalDateTime scheduledTime = DateTimeUtil.combineWithDate(reservedDate, reservedTime).minusHours(1);

        // 알림 메세지 data 별 포멧팅    
        String title = NotificationMessageUtil.formatHourBeforeNotificationTitle(userName, restaurantName);
        String body = NotificationMessageUtil.formatHourBeforeNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationMessageUtil.formatReservationNotificationUrl(reservationId);

        // 알림 메세지 빌드
        ReservationNotification reservationNotification = ReservationNotification.builder()
                .type(NotificationType.BEFORE_ONE_HOUR_NOTIFICATION)
                .title(title)
                .body(body)
                .url(url)
                .user(user)
                .scheduledTime(scheduledTime)
                .reservation(reservation)
                .build();

        return reservationNotification;
    }

    /**
     * 예약 취소 시, 해당 예약에 대한 알림을 취소하는 메서드
     *
     * @param reservationId 예약 ID
     */
    public void deleteReservationNotification(Long reservationId) {
        reservationNotificationRepository.deleteAllByReservation(reservationId);
    }

    /**
     * 매일 8시에 당일 예약 알림을 보내는 메서드
     */
    public void sendDailyNotification() {
        LocalDate today = LocalDate.now();

        // 알림 타입과 오늘 날짜로 당일 예약 알림 정보 가져오기
        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndDate(NotificationType.DAILY_NOTIFICATION, today);

        // history 테이블과 알림 발송 후 알림 메세지 삭제를 위한 알림 ID 담을 list 세팅
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        // 알림 타입 세팅
        NotificationType type = NotificationType.DAILY_NOTIFICATION;

        // 전송할 알림 리스트를 전부 도는 알림 발송 로직(현재 동기 처리 중)
        for (ReservationNotification notification : reservationNotificationList) {
            // 단 건의 알림 전송 로직에 대해 처리하는 메서드
            NotificationHistory notificationHistory = handleEachNotification(notification, type);
            notificationHistoryList.add(notificationHistory);
            reservationNotificationIdList.add(notification.getId());
        }
        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        saveNotificationHistory(notificationHistoryList);
        // 전송한 예약 알림을 table에서 삭제하는 메서드
        deleteSentReservationNotification(reservationNotificationIdList);
    }

    /**
     * 예약 1시간 전 알림을 보내는 메서드
     */
    public void sendHourBeforeNotification() {
        LocalDateTime now = DateTimeUtil.truncateToMinute(LocalDateTime.now());

        // 알림 타입과 현재 시간으로 보낼 예약 1시간 전 알림 정보 가져오기
        List<ReservationNotification> reservationNotificationList = reservationNotificationRepository
                .findAllByTypeAndDateTime(NotificationType.BEFORE_ONE_HOUR_NOTIFICATION, now);

        // history 테이블과 알림 발송 후 알림 메세지 삭제를 위한 알림 ID 담을 list 세팅
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        // 알림 타입 세팅
        NotificationType type = NotificationType.BEFORE_ONE_HOUR_NOTIFICATION;

        // 전송할 알림 리스트를 전부 도는 알림 발송 로직(현재 동기 처리 중)
        for (ReservationNotification notification : reservationNotificationList) {
            NotificationHistory notificationHistory = handleEachNotification(notification, type);
            notificationHistoryList.add(notificationHistory);
            reservationNotificationIdList.add(notification.getId());
        }
        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        saveNotificationHistory(notificationHistoryList);
        // 전송한 예약 알림을 table에서 삭제하는 메서드
        deleteSentReservationNotification(reservationNotificationIdList);
    }

    /**
     * 단 건의 알림 전송 로직에 대해 처리하는 메서드
     *
     * @param notification 예약 알림 Entity
     * @param type         알림 타입(ENUM)
     * @return
     */
    private NotificationHistory handleEachNotification(ReservationNotification notification, NotificationType type) {
        // 보낼 FCM 메세지 빌드
        Message message = buildReservationNotification(notification);
        // FCM 메세지 전송 및 전송 결과 반환
        FcmSendingResultDto result = sendEachNotification(message);
        return buildNotificationHistory(notification, result, type);
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 빌드하는 내부 메서드
     *
     * @param notification 예약 알림 Entity
     * @return
     */
    private Message buildReservationNotification(ReservationNotification notification) {
        // FCM 토큰 가져오기
        String token = fcmRepository.findByUser(notification.getUser().getId());

        // FCM 메시지 빌드
        Message message = Message.builder()
                .putData("title", notification.getTitle())
                .putData("body", notification.getBody())
                .putData("url", notification.getUrl())
                .setToken(token)
                .build();

        return message;
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 발송하는 내부 메서드
     *
     * @param message FCM 메세지 객체
     * @return
     */
    private FcmSendingResultDto sendEachNotification(Message message) {
        // FCM 메세지 전송 결과를 담는 DTO
        FcmSendingResultDto result;

        try {
            // FCM 서버에 메세지 전송
            FirebaseMessaging.getInstance().send(message);
            // 전송 결과(전송 시간, 전송 상태)
            result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
        } catch (FirebaseMessagingException e) {
            // 전송 결과(전송 시간, 전송 상태, 에러 코드)
            result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, e.getMessagingErrorCode());
        }
        return result;
    }

    /**
     * 알림 전송 로직 중 전송된 알림에 대한 히스토리를 빌드하는 내부 메서드
     *
     * @param notification 예약 알림 Entity
     * @param result       FCM 메세지 전송 결과 DTO
     * @param type         알림 타입
     * @return
     */
    private NotificationHistory buildNotificationHistory(ReservationNotification notification,
                                                         FcmSendingResultDto result, NotificationType type) {
        // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
        Map<String, String> messageHistory = new HashMap<>();
        messageHistory.put("title", notification.getTitle());
        messageHistory.put("body", notification.getBody());
        messageHistory.put("url", notification.getUrl());

        // 알림 history 빌드
        NotificationHistory notificationHistory = NotificationHistory.builder()
                .type(type)
                .message(messageHistory)
                .sentAt(result.getSentAt())
                .sentResult(result.getSentResult())
                .errorCode(result.getErrorCode())
                .user(notification.getUser())
                .build();
        return notificationHistory;
    }

    /**
     * 전송된 알림 히스토리를 전부 history 테이블에 저장
     * @param notificationHistoryList 알림 history Entity 리스트
     */
    private void saveNotificationHistory(List<NotificationHistory> notificationHistoryList) {
        notificationHistoryRepository.saveAll(notificationHistoryList);
    }

    /**
     * 전송한 예약 알림을 table에서 삭제하는 메서드
     * @param reservationNotificationIdList
     */
    private void deleteSentReservationNotification(List<Long> reservationNotificationIdList) {
        reservationNotificationRepository.deleteAllById(reservationNotificationIdList);
    }

    @Transactional
    public void subscribeSeatNotification(Long seatId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 티켓 개수 검증, 509 반환
        TicketValidator.validateEmptyTicketUsage(user.getEmptyTicketCount());

        SeatNotificationMessage seatNotificationMessage = seatNotificationMessageRepository.findBySeatId(seatId)
                .orElseGet(() -> registerSeatNotificationMessage(seatId));

        // 이미 신청 내역이 있을 시, 409 반환
        seatNotificationRepository.findByUserId(userId, seatNotificationMessage)
                .ifPresent(notification -> {
                    log.info("이미 신청 내역이 존재합니다. userId: {}", userId);
                    throw new CustomException(NotificationErrorCode.ALREADY_SUBSCRIBED);
                });

        SeatNotification seatNotification = SeatNotification.builder()
                .user(user)
                .seatNotificationMessage(seatNotificationMessage)
                .build();

        seatNotificationRepository.save(seatNotification);
    }

    private SeatNotificationMessage registerSeatNotificationMessage(long seatId) {
        SeatAvailability seat = seatAvailabilityRepository.findById(seatId).orElseThrow(() -> new CustomException(SeatErrorCode.NOT_FOUND));

        // 빈자리 알림 메세지에 필요한 정보 변수 선언
        LocalDate date = seat.getReservationDate();
        LocalTime time = seat.getReservationTime().getTimeSlot();
        long restaurantId = seat.getRestaurant().getId();
        String restaurantName = seat.getRestaurant().getName();
        int minCapacity = seat.getSeatType().getMinCapacity();
        int maxCapacity = seat.getSeatType().getMaxCapacity();

        // 알림 메세지 data 별 포멧팅
        String title = NotificationMessageUtil.formatSeatNotificationTitle(restaurantName);
        String body = NotificationMessageUtil.formatSeatNotificationBody(date, time, minCapacity, maxCapacity);
        String url = NotificationMessageUtil.formatSeatNotificationUrl(restaurantId);

        // 알림 메세지 빌드
        SeatNotificationMessage message = SeatNotificationMessage.builder()
                .title(title)
                .body(body)
                .url(url)
                .seatAvailability(seat)
                .build();

        return seatNotificationMessageRepository.save(message);
    }

    @Transactional
    public void cancelSubscribeSeatNotification(Long seatNotificationId) {
        seatNotificationRepository.deleteById(seatNotificationId);
    }

    @Transactional
    public SubscriptionListResponse getSeatNotifications(long userId) {
        List<SeatNotification> seatNotificationList = seatNotificationRepository.findAllByUserId(userId);
        List<SubscriptionResponse> subscriptionResponseList = new ArrayList<>();
        log.info("조회된 SeatNotification 개수: {}", seatNotificationList.size());


        for(SeatNotification notification : seatNotificationList) {
            int subscriberCount = seatNotificationRepository.countBySeatNotificationMessage(notification.getSeatNotificationMessage());

            log.info("SeatNotification ID: {}, 관련 SeatAvailability ID: {}, 구독자 수: {}",
                    notification.getId(),
                    notification.getSeatNotificationMessage().getSeatAvailability().getId(),
                    subscriberCount);

            SubscriptionResponse subscriptionResponse = SubscriptionResponse.of(notification.getId(), notification.getSeatNotificationMessage().getSeatAvailability(), subscriberCount);
            subscriptionResponseList.add(subscriptionResponse);
            log.info("response: " + subscriptionResponse.getSeatNotificationId());
        }

        SubscriptionListResponse subscriptionListResponse = new SubscriptionListResponse(subscriptionResponseList.size(), subscriptionResponseList);

        return subscriptionListResponse;
    }

    // 빈자리 알림 발송 시작점
    @Transactional
    public void sendSeatNotification(long seatId) {
        // 빈자리 알림 메세지 정보(구독한 빈자리 알림)
        SeatNotificationMessage seatNotificationMessage = seatNotificationMessageRepository.findBySeatId(seatId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOT_FOUND));

        // 빈자리 알림 정보 가져오기(구독자 정보)
        List<SeatNotification> seatNotificationList = seatNotificationRepository.findAllBySeatId(seatId);

        // 알림 타입 세팅
        NotificationType type = NotificationType.SEAT_NOTIFICATION;

        List<FcmSendingResultDto> resultList = handleMulticastNotification(seatNotificationMessage, seatNotificationList);


        List<NotificationHistory> notificationHistoryList = buildMulticastNotificationHistory(seatNotificationList, seatNotificationMessage, type, resultList);

        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        saveNotificationHistory(notificationHistoryList);
    }

    private List<FcmSendingResultDto> handleMulticastNotification(SeatNotificationMessage seatNotificationMessage, List<SeatNotification> seatNotificationList) {
        MulticastMessage multicastMessage = buildSeatNotifications(seatNotificationMessage, seatNotificationList);

        // FCM 메세지 전송 및 전송 결과 반환
        List<FcmSendingResultDto> resultList = sendMulticastNotification(multicastMessage);


        return resultList;
    }

    private MulticastMessage buildSeatNotifications(SeatNotificationMessage seatNotificationMessage, List<SeatNotification> seatNotificationList) {
        List<String> tokenList = new ArrayList<>();
        for(SeatNotification notification : seatNotificationList) {
            String token = fcmRepository.findByUser(notification.getUser().getId());
            tokenList.add(token);
        }

        // FCM 메시지 빌드
        MulticastMessage message = MulticastMessage.builder()
                .putData("title", seatNotificationMessage.getTitle())
                .putData("body", seatNotificationMessage.getBody())
                .putData("url", seatNotificationMessage.getUrl())
                .addAllTokens(tokenList)
                .build();

        return message;
    }

    private List<FcmSendingResultDto> sendMulticastNotification(MulticastMessage message) {
        // FCM 메세지 전송 결과를 담는 DTO
        List<FcmSendingResultDto> resultList = new ArrayList<>();

        try {
            // FCM 서버에 메세지 전송
            List<SendResponse> sendResponseList = FirebaseMessaging.getInstance().sendEachForMulticast(message).getResponses();
            // 전송 결과(전송 시간, 전송 상태)
            for(int i = 0; i < sendResponseList.size(); i++) {
                SendResponse sendResponse = sendResponseList.get(i);
                LocalDateTime time = LocalDateTime.now();

                if(sendResponse.isSuccessful()){
                    FcmSendingResultDto fcmSendingResultDto = new FcmSendingResultDto(time, SentResult.SUCCESS);
                } else {
                    FcmSendingResultDto fcmSendingResultDto = new FcmSendingResultDto(time, SentResult.FAILED, sendResponse.getException().getMessagingErrorCode());
                }
            }
        } catch (FirebaseMessagingException e) {
            // 전송 결과(전송 시간, 전송 상태, 에러 코드
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }

        return resultList;
    }


    private List<NotificationHistory> buildMulticastNotificationHistory(List<SeatNotification> seatNotificationList,
                                                                        SeatNotificationMessage seatNotificationMessage,
                                                                        NotificationType type, List<FcmSendingResultDto> resultList) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
        Map<String, String> messageHistory = new HashMap<>();
        messageHistory.put("title", seatNotificationMessage.getTitle());
        messageHistory.put("body", seatNotificationMessage.getBody());
        messageHistory.put("url", seatNotificationMessage.getUrl());

        for(int i = 0; i < seatNotificationList.size(); i++) {
            NotificationHistory notificationHistory = NotificationHistory.builder()
                    .type(type)
                    .message(messageHistory)
                    .sentAt(resultList.get(i).getSentAt())
                    .sentResult(resultList.get(i).getSentResult())
                    .errorCode(resultList.get(i).getErrorCode())
                    .user(seatNotificationList.get(i).getUser())
                    .build();

            notificationHistoryList.add(notificationHistory);
        }
        // 알림 history 빌드

        return notificationHistoryList;
    }

    /**
     * 예약 알림 테스트 메서드(mock test 코드 작성 후 삭제 예정)
     * @param userId
     * @param reservationId
     */
    public void testReservationNotification(long userId, long reservationId) {
        eventPublisher.publishEvent(new ReservationCompleteEvent(userId, reservationId));
    }

    /**
     * 빈자리 알림 테스트 메서드(mock test 코드 작성 후 삭제 예정)
     * @param reservationId
     */
    public void testSeatNotification(long reservationId) {
        reservationService.cancelReservation(reservationId);
    }

    /**
     * 날짜/시간이 지난 자리에 대한 빈자리 알림 메세지/빈자리 알림 신청에 대한 데이터 삭제 메서드
     */
    @Transactional
    public void deleteSeatNotificationMessages() {
        // 현재 날짜/시간 포멧팅
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = DateTimeUtil.truncateTimeToMinute(LocalTime.now());

        // 현재 날짜/시간 기준으로 이전의 자리에 해당하는 빈자리 알림 메세지를 select
        List<SeatNotificationMessage> messages = seatNotificationMessageRepository.findAllByCurrentDateTime(currentDate, currentTime);
        // 삭제(Cascade 설정으로 알림 신청 데이터도 삭제됨)
        seatNotificationMessageRepository.deleteAll(messages);
    }
}
