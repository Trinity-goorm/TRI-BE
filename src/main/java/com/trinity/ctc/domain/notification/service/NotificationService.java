package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.*;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.notification.dto.*;
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
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.repository.SeatRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.*;
import com.trinity.ctc.util.formatter.DateTimeUtil;
import com.trinity.ctc.util.validator.TicketValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
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
    private final SeatRepository seatRepository;
    private final SeatNotificationMessageRepository seatNotificationMessageRepository;
    private final SeatNotificationRepository seatNotificationRepository;
    private final ReservationService reservationService;

    /**
     * 예약 이벤트를 통해 예약 알림에 필요한 entity(user, reservation)를 받아오고, 예약 알림 entity을 DB에 저장하는 메서드
     *
     * @param userId        사용자
     * @param reservationId 예약 정보
     */
    @Transactional
    public void registerReservationNotification(long userId, long reservationId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

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
        LocalDateTime scheduledTime = combineWithDate(reservedDate, LocalTime.of(8, 0));

        // 알림 메세지 data 별 포멧팅
        String title = NotificationMessageUtil.formatDailyNotificationTitle(userName, restaurantName);
        String body = NotificationMessageUtil.formatDailyNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationMessageUtil.formatReservationNotificationUrl();

        // 알림 메세지 빌드

        return ReservationNotification.builder()
                .type(NotificationType.DAILY_NOTIFICATION)
                .title(title)
                .body(body)
                .url(url)
                .user(user)
                .scheduledTime(scheduledTime)
                .reservation(reservation)
                .build();
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
        LocalDateTime scheduledTime = DateTimeUtil.combineWithDate(reservedDate, reservedTime).minusHours(1);

        // 알림 메세지 data 별 포멧팅    
        String title = NotificationMessageUtil.formatHourBeforeNotificationTitle(userName, restaurantName);
        String body = NotificationMessageUtil.formatHourBeforeNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationMessageUtil.formatReservationNotificationUrl();

        // 알림 메세지 빌드

        return ReservationNotification.builder()
                .type(NotificationType.BEFORE_ONE_HOUR_NOTIFICATION)
                .title(title)
                .body(body)
                .url(url)
                .user(user)
                .scheduledTime(scheduledTime)
                .reservation(reservation)
                .build();
    }

    /**
     * 예약 취소 시, 해당 예약에 대한 알림을 취소하는 메서드
     *
     * @param reservationId 예약 ID
     */
    @Transactional
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
            List<NotificationHistory> notificationHistory = handleEachNotification(notification, type);
            notificationHistoryList.addAll(notificationHistory);
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
            List<NotificationHistory> notificationHistory = handleEachNotification(notification, type);
            notificationHistoryList.addAll(notificationHistory);
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
    private List<NotificationHistory> handleEachNotification(ReservationNotification notification, NotificationType type) {
        // 보낼 FCM 메세지 빌드
        GroupFcmInformationDto fcmInformationDto = buildReservationNotification(notification);
        // FCM 메세지 전송 및 전송 결과 반환
        List<FcmSendingResultDto> resultList = sendEachNotification(fcmInformationDto.getMessageList());
        return buildNotificationHistory(fcmInformationDto.getMessageDtoList(), resultList, type);
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 빌드하는 내부 메서드
     *
     * @param notification 예약 알림 Entity
     * @return
     */
    private GroupFcmInformationDto buildReservationNotification(ReservationNotification notification) {
        // FCM 토큰 가져오기
        List<String> tokenList = fcmRepository.findByUser(notification.getUser().getId());

        List<Message> messageList = new ArrayList<>();
        List<FcmMessageDto> messageDtoList = new ArrayList<>();
        // FCM 메시지 빌드

        for (String token : tokenList) {
            messageList.add(Message.builder()
                    .putData("title", notification.getTitle())
                    .putData("body", notification.getBody())
                    .putData("url", notification.getUrl())
                    .setToken(token)
                    .build());

            messageDtoList.add(new FcmMessageDto(token, notification.getTitle(), notification.getBody(), notification.getUrl(), notification.getUser()));
        }

        return new GroupFcmInformationDto(messageDtoList, messageList);
    }

    /**
     * 알림 전송 로직 중 FCM 메세지를 발송하는 내부 메서드
     *
     * @param messageList FCM 메세지 객체 리스트
     * @return
     */
    private List<FcmSendingResultDto> sendEachNotification(List<Message> messageList) {
        // FCM 메세지 전송 결과를 담는 DTO
        FcmSendingResultDto result;
        List<FcmSendingResultDto> resultList = new ArrayList<>();

        for (Message message : messageList) {
            try {
                // FCM 서버에 메세지 전송
                FirebaseMessaging.getInstance().send(message);
                // 전송 결과(전송 시간, 전송 상태)
                result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.SUCCESS);
            } catch (FirebaseMessagingException e) {
                // 전송 결과(전송 시간, 전송 상태, 에러 코드)
                result = new FcmSendingResultDto(LocalDateTime.now(), SentResult.FAILED, e.getMessagingErrorCode());
            }
            resultList.add(result);
        }

        return resultList;
    }

    /**
     * 알림 전송 로직 중 전송된 알림에 대한 히스토리를 빌드하는 내부 메서드
     *
     * @param messageDtoList FCM 메세지 정보 DTO 리스트
     * @param resultList     FCM 메세지 전송 결과 DTO 리스트
     * @param type           알림 타입
     * @return
     */
    private List<NotificationHistory> buildNotificationHistory(List<FcmMessageDto> messageDtoList,
                                                               List<FcmSendingResultDto> resultList, NotificationType type) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        for (int i = 0; i < messageDtoList.size(); i++) {
            // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
            Map<String, String> messageHistory = new HashMap<>();
            messageHistory.put("title", messageDtoList.get(i).getTitle());
            messageHistory.put("body", messageDtoList.get(i).getBody());
            messageHistory.put("url", messageDtoList.get(i).getUrl());

            // 알림 history 빌드
            notificationHistoryList.add(NotificationHistory.builder()
                    .type(type)
                    .message(messageHistory)
                    .sentAt(resultList.get(i).getSentAt())
                    .sentResult(resultList.get(i).getSentResult())
                    .errorCode(resultList.get(i).getErrorCode())
                    .fcmToken(messageDtoList.get(i).getFcmToken())
                    .user(messageDtoList.get(i).getUser())
                    .build());
        }

        return notificationHistoryList;
    }

    /**
     * 전송된 알림 히스토리를 전부 history 테이블에 저장
     *
     * @param notificationHistoryList 알림 history Entity 리스트
     */
    private void saveNotificationHistory(List<NotificationHistory> notificationHistoryList) {
        notificationHistoryRepository.saveAll(notificationHistoryList);
    }

    /**
     * 전송한 예약 알림을 table에서 삭제하는 메서드
     *
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

        // 빈자리 알림 신청 내역 build
        SeatNotification seatNotification = SeatNotification.builder()
                .user(user)
                .seatNotificationMessage(seatNotificationMessage)
                .build();

        // 빈자리 알림 신청 시, 빈자리 알림 티켓 -1
        user.useEmptyTicket();

        // 빈자리 알림 신청 내역 저장
        seatNotificationRepository.save(seatNotification);
    }

    /**
     * 빈자리 알림 최초 신청 시, 구독 message를 등록하는 메서드
     * @param seatId
     * @return
     */
    private SeatNotificationMessage registerSeatNotificationMessage(long seatId) {
        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new CustomException(SeatErrorCode.NOT_FOUND));

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
                .seat(seat)
                .build();

        return seatNotificationMessageRepository.save(message);
    }

    /**
     * 빈자리 알림 신청 취소 메서드
     * @param seatNotificationId
     */
    @Transactional
    public void cancelSubscribeSeatNotification(Long seatNotificationId) {
        seatNotificationRepository.deleteById(seatNotificationId);
    }

    /**
     * 사용자의 빈자리 알림 신청 내역 반환 메서드
     * @param userId
     * @return
     */
    @Transactional
    public SubscriptionListResponse getSeatNotifications(long userId) {
        List<SeatNotification> seatNotificationList = seatNotificationRepository.findAllByUserId(userId);
        List<SubscriptionResponse> subscriptionResponseList = new ArrayList<>();
        log.info("조회된 SeatNotification 개수: {}", seatNotificationList.size());


        for (SeatNotification notification : seatNotificationList) {
            int subscriberCount = seatNotificationRepository.countBySeatNotificationMessage(notification.getSeatNotificationMessage());

            log.info("SeatNotification ID: {}, 관련 Seat ID: {}, 구독자 수: {}",
                    notification.getId(),
                    notification.getSeatNotificationMessage().getSeat().getId(),
                    subscriberCount);

            SubscriptionResponse subscriptionResponse = SubscriptionResponse.of(notification.getId(), notification.getSeatNotificationMessage().getSeat(), subscriberCount);
            subscriptionResponseList.add(subscriptionResponse);
            log.info("response: " + subscriptionResponse.getSeatNotificationId());
        }

        return new SubscriptionListResponse(subscriptionResponseList.size(), subscriptionResponseList);
    }

    // 빈자리 알림 발송 시작점

    /**
     * 빈자리에 대한 예약 취소 이벤트 발생 시, 빈자리 알림을 발송하는 메서드 
     * @param seatId
     */
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

    /**
     * multicastMessage를 처리하는 메서드
     * @param seatNotificationMessage
     * @param seatNotificationList
     * @return
     */
    private List<FcmSendingResultDto> handleMulticastNotification(SeatNotificationMessage seatNotificationMessage, List<SeatNotification> seatNotificationList) {
        MulticastMessage multicastMessage = buildSeatNotifications(seatNotificationMessage, seatNotificationList);

        // FCM 메세지 전송 및 전송 결과 반환
        return sendMulticastNotification(multicastMessage);
    }

    /**
     * 빈자리 알림 FCM 메세지릴 build하는 메서드
     * @param seatNotificationMessage
     * @param seatNotificationList
     * @return
     */
    private MulticastMessage buildSeatNotifications(SeatNotificationMessage seatNotificationMessage, List<SeatNotification> seatNotificationList) {
        List<String> tokenList = new ArrayList<>();
        for (SeatNotification notification : seatNotificationList) {
            List<String> userTokens = fcmRepository.findByUser(notification.getUser().getId());
            tokenList.addAll(userTokens);
        }

        // FCM 메시지 빌드 후 반환
        return MulticastMessage.builder()
                .putData("title", seatNotificationMessage.getTitle())
                .putData("body", seatNotificationMessage.getBody())
                .putData("url", seatNotificationMessage.getUrl())
                .addAllTokens(tokenList)
                .build();
    }

    /**
     * MulticastMessage를 발송하는 내부 메서드
     * @param message
     * @return
     */
    private List<FcmSendingResultDto> sendMulticastNotification(MulticastMessage message) {
        // FCM 메세지 전송 결과를 담는 DTO
        List<FcmSendingResultDto> resultList = new ArrayList<>();

        try {
            // FCM 서버에 메세지 전송
            List<SendResponse> sendResponseList = FirebaseMessaging.getInstance().sendEachForMulticast(message).getResponses();
            // 전송 결과(전송 시간, 전송 상태)
            for (SendResponse sendResponse : sendResponseList) {
                LocalDateTime time = LocalDateTime.now();

                if (sendResponse.isSuccessful()) {
                    resultList.add(new FcmSendingResultDto(time, SentResult.SUCCESS));
                } else {
                    resultList.add(new FcmSendingResultDto(time, SentResult.FAILED, sendResponse.getException().getMessagingErrorCode()));
                }
            }
        } catch (FirebaseMessagingException e) {
            // 전송 결과(전송 시간, 전송 상태, 에러 코드
            throw new CustomException(FcmErrorCode.SENDING_REQUEST_FAILED);
        }

        return resultList;
    }

    /**
     * Multicast Message의 알림 history 데이터를 build 하는 메서드
     * @param seatNotificationList
     * @param seatNotificationMessage
     * @param type
     * @param resultList
     * @return
     */
    private List<NotificationHistory> buildMulticastNotificationHistory(List<SeatNotification> seatNotificationList,
                                                                        SeatNotificationMessage seatNotificationMessage,
                                                                        NotificationType type, List<FcmSendingResultDto> resultList) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
        Map<String, String> messageHistory = new HashMap<>();
        messageHistory.put("title", seatNotificationMessage.getTitle());
        messageHistory.put("body", seatNotificationMessage.getBody());
        messageHistory.put("url", seatNotificationMessage.getUrl());

        for (int i = 0; i < seatNotificationList.size(); i++) {
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
     * 빈자리 알림 테스트 메서드(mock test 코드 작성 후 삭제 예정)
     *
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


    /**
     * 예약 완료 알림 전송 메서드
     *
     * @param userId
     * @param reservationId
     */
    @Transactional
    public void sendReservationSuccessNotification(Long userId, Long reservationId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));

        FcmMessageDto messageData = formattingReservationCompleteNotification(reservation);
        GroupFcmInformationDto groupFcmInformationDto = buildMessageList(user, messageData);
        List<Message> messageList = groupFcmInformationDto.getMessageList();
        List<FcmMessageDto> messageDtoList = groupFcmInformationDto.getMessageDtoList();

        // 알림 타입 세팅
        NotificationType type = NotificationType.RESERVATION_COMPLETE;

        // 단 건의 알림 전송 로직에 대해 처리하는 메서드
        List<NotificationHistory> notificationHistoryList = sendSingleNotification(messageList, type, messageDtoList);

        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 예약 완료 알림 메세지를 포멧팅하는 내부 메서드
     * @param reservation
     * @return
     */
    private FcmMessageDto formattingReservationCompleteNotification(Reservation reservation) {
        // 예약 완료 알림 메세지에 필요한 정보 변수 선언
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();
        int minCapacity = reservation.getSeatType().getMinCapacity();
        int maxCapacity = reservation.getSeatType().getMaxCapacity();

        // 알림 메세지 data 별 포멧팅
        String title = NotificationMessageUtil.formatReservationCompleteNotificationTitle(restaurantName);
        String body = NotificationMessageUtil.formatReservationCompleteNotificationBody(reservedDate, reservedTime, minCapacity,
                maxCapacity);
        String url = NotificationMessageUtil.formatReservationNotificationUrl();

        return new FcmMessageDto(title, body, url);
    }

    /**
     * FCM 메세지 리스트를 build하는 내부 메서드
     * @param user
     * @param fcmMessageDto
     * @return
     */
    private GroupFcmInformationDto buildMessageList(User user, FcmMessageDto fcmMessageDto) {
        List<Fcm> tokenList = user.getFcmList();
        log.info(tokenList.size() + " tokens");
        log.info("token:" + tokenList.get(0).getToken());
        List<Message> messageList = new ArrayList<>();
        List<FcmMessageDto> fcmMessageDtoList = new ArrayList<>();
        Message message;

        for (Fcm token : tokenList) {
            message = Message.builder()
                    .putData("title", fcmMessageDto.getTitle())
                    .putData("body", fcmMessageDto.getBody())
                    .putData("url", fcmMessageDto.getUrl())
                    .setToken(token.getToken())
                    .build();

            messageList.add(message);

            fcmMessageDtoList.add(FcmMessageDto.of(fcmMessageDto, token.getToken(), user));
        }

        return new GroupFcmInformationDto(fcmMessageDtoList, messageList);
    }


    /**
     * 하나의 사용자를 대상으로 하는 단 건 알림을 발송하는 내부 메서드
     * @param messageList
     * @param type
     * @param fcmMessageDtoList
     * @return
     */
    private List<NotificationHistory> sendSingleNotification(List<Message> messageList, NotificationType type, List<FcmMessageDto> fcmMessageDtoList) {
        // FCM 메세지 전송 및 전송 결과 반환
        List<FcmSendingResultDto> resultList = sendEachNotification(messageList);
        return buildNotificationHistory(fcmMessageDtoList, resultList, type);
    }

    /**
     * 예약 취소 알림 전송 메서드
     *
     * @param userId
     * @param reservationId
     * @param isCODPassed
     */
    @Transactional
    public void sendReservationCanceledNotification(Long userId, Long reservationId, boolean isCODPassed) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new CustomException(ReservationErrorCode.NOT_FOUND));
        FcmMessageDto messageData = formattingReservationCanceledNotification(reservation, user, isCODPassed);
        GroupFcmInformationDto groupFcmInformationDto = buildMessageList(user, messageData);

        List<Message> messageList = groupFcmInformationDto.getMessageList();
        List<FcmMessageDto> messageDtoList = groupFcmInformationDto.getMessageDtoList();

        // 알림 타입 세팅
        NotificationType type = NotificationType.RESERVATION_CANCELED;

        // 단 건의 알림 전송 로직에 대해 처리하는 메서드
        List<NotificationHistory> notificationHistoryList = sendSingleNotification(messageList, type, messageDtoList);

        // 전송된 알림 히스토리를 전부 history 테이블에 저장하는 메서드
        saveNotificationHistory(notificationHistoryList);
    }

    /**
     * 예약 취소 메세지를 포멧팅하는 내부 메서드
     * @param reservation
     * @param user
     * @param isCODPassed
     * @return
     */
    private FcmMessageDto formattingReservationCanceledNotification(Reservation reservation, User user, boolean isCODPassed) {
        // 예약 완료 알림 메세지에 필요한 정보 변수 선언
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservedDate = reservation.getReservationDate();
        LocalTime reservedTime = reservation.getReservationTime().getTimeSlot();

        String title;
        String body;

        // 알림 메세지 data 별 포멧팅
        if (isCODPassed) {
            title = NotificationMessageUtil.formatReservationFullCanceledNotificationTitle(restaurantName);
            body = NotificationMessageUtil.formatReservationFullCanceledNotificationBody(reservedDate, reservedTime, user.getNormalTicketCount());
        } else {
            title = NotificationMessageUtil.formatReservationNullCanceledNotificationTitle(restaurantName);
            body = NotificationMessageUtil.formatReservationNullCanceledNotificationBody(reservedDate, reservedTime, user.getNormalTicketCount());
        }

        String url = NotificationMessageUtil.formatReservationNotificationUrl();

        return new FcmMessageDto(title, body, url);
    }

    /**
     * 예약 완료 알림 테스트용 메서드 (mock test 코드 작성 후 삭제 예정)
     * @param userId
     * @param reservationId
     */
    @Transactional
    public void testReservationNotification(long userId, long reservationId) {
        sendReservationSuccessNotification(userId, reservationId);
    }
}
