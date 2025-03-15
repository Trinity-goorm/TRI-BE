package com.trinity.ctc.domain.notification.service;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.fcm.repository.FcmRepository;
import com.trinity.ctc.domain.notification.dto.*;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.fomatter.NotificationContentUtil;
import com.trinity.ctc.domain.notification.repository.NotificationHistoryRepository;
import com.trinity.ctc.domain.notification.repository.ReservationNotificationRepository;
import com.trinity.ctc.domain.notification.repository.SeatNotificationRepository;
import com.trinity.ctc.domain.notification.repository.SeatNotificationSubscriptionRepository;
import com.trinity.ctc.domain.notification.sender.NotificationSender;
import com.trinity.ctc.domain.notification.type.NotificationType;
import com.trinity.ctc.domain.notification.validator.EmptyTicketValidator;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.reservation.status.ReservationStatus;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.repository.SeatRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.*;
import com.trinity.ctc.global.kakao.service.AuthService;
import com.trinity.ctc.global.util.formatter.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static com.trinity.ctc.domain.notification.entity.NotificationHistory.createNotificationHistory;
import static com.trinity.ctc.domain.notification.entity.ReservationNotification.createReservationNotification;
import static com.trinity.ctc.domain.notification.entity.SeatNotification.createSeatNotification;
import static com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription.createSeatNotificationSubscription;
import static com.trinity.ctc.domain.notification.fomatter.NotificationContentUtil.*;
import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMessageWithUrl;
import static com.trinity.ctc.domain.notification.fomatter.NotificationMessageUtil.createMulticastMessageWithUrl;
import static com.trinity.ctc.domain.notification.type.NotificationType.BEFORE_ONE_HOUR_NOTIFICATION;
import static com.trinity.ctc.domain.notification.type.NotificationType.DAILY_NOTIFICATION;
import static com.trinity.ctc.global.util.formatter.DateTimeUtil.combineWithDate;

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
    private final SeatNotificationRepository seatNotificationRepository;
    private final SeatNotificationSubscriptionRepository seatNotificationSubscriptionRepository;
    private final AuthService authService;
    private final NotificationSender notificationSender;

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
        String title = NotificationContentUtil.formatDailyNotificationTitle(userName, restaurantName);
        String body = NotificationContentUtil.formatDailyNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationContentUtil.formatReservationNotificationUrl();

        // 알림 메세지 빌드
        return createReservationNotification(DAILY_NOTIFICATION, user, reservation, title, body, url, scheduledTime);
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
        String title = NotificationContentUtil.formatHourBeforeNotificationTitle(userName, restaurantName);
        String body = NotificationContentUtil.formatHourBeforeNotificationBody(restaurantName, reservedDate, reservedTime);
        String url = NotificationContentUtil.formatReservationNotificationUrl();

        // 알림 메세지 빌드

        return createReservationNotification(BEFORE_ONE_HOUR_NOTIFICATION, user, reservation, title, body, url, scheduledTime);
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
                .findAllByTypeAndDate(DAILY_NOTIFICATION, today);
        if (reservationNotificationList.isEmpty()) return;

        // history 테이블과 알림 발송 후 알림 메세지 삭제를 위한 알림 ID 담을 list 세팅
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        // 알림 타입 세팅
        NotificationType type = DAILY_NOTIFICATION;

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
                .findAllByTypeAndDateTime(BEFORE_ONE_HOUR_NOTIFICATION, now);

        // history 테이블과 알림 발송 후 알림 메세지 삭제를 위한 알림 ID 담을 list 세팅
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();
        List<Long> reservationNotificationIdList = new ArrayList<>();

        // 알림 타입 세팅
        NotificationType type = BEFORE_ONE_HOUR_NOTIFICATION;

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
        List<FcmSendingResultDto> resultList = notificationSender.sendEachNotification(fcmInformationDto.getMessageList());
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
        if (tokenList.isEmpty()) throw new CustomException(FcmErrorCode.NO_FCM_TOKEN_REGISTERED);

        List<Message> messageList = new ArrayList<>();
        List<FcmMessageDto> messageDtoList = new ArrayList<>();
        // FCM 메시지 빌드

        for (String token : tokenList) {
            messageList.add(createMessageWithUrl(notification.getTitle(), notification.getBody(), notification.getUrl(), token));

            messageDtoList.add(new FcmMessageDto(token, notification.getTitle(), notification.getBody(), notification.getUrl(), notification.getUser()));
        }

        return new GroupFcmInformationDto(messageDtoList, messageList);
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
            notificationHistoryList.add(createNotificationHistory(type, messageHistory, resultList.get(i).getSentAt(), resultList.get(i).getSentResult(),
                    resultList.get(i).getErrorCode(), messageDtoList.get(i).getFcmToken(), messageDtoList.get(i).getUser()));
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
    public void subscribeSeatNotification(Long seatId) {
        String kakaoId = authService.getAuthenticatedKakaoId();

        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId)).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 티켓 개수 검증, 509 반환
        EmptyTicketValidator.validateEmptyTicketUsage(user.getEmptyTicketCount());

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
    @Transactional
    public SubscriptionListResponse getSeatNotifications() {
        String kakaoId = authService.getAuthenticatedKakaoId();
        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId))
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        int pageNumber = 0;
        int pageSize = 500;
        List<SubscriptionResponse> subscriptionResponseList = new ArrayList<>();

        Page<SeatNotificationSubscription> page;

        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
            page = seatNotificationSubscriptionRepository.findAllByUserId(user.getId(), pageable);
            List<SeatNotificationSubscription> seatNotificationSubscriptionList = page.getContent();

            log.info("조회된 SeatNotification 개수 (페이지 {}): {}", pageNumber, seatNotificationSubscriptionList.size());

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

            pageNumber++; // 다음 페이지로 이동
        } while (page.hasNext()); // 다음 페이지가 있으면 반복

        return new SubscriptionListResponse(subscriptionResponseList.size(), subscriptionResponseList);
    }

    // 빈자리 알림 발송 시작점

    /**
     * 빈자리에 대한 예약 취소 이벤트 발생 시, 빈자리 알림을 발송하는 메서드
     *
     * @param seatId
     */
    @Transactional
    public void sendSeatNotification(long seatId) {
        log.info("✅ 빈자리 알림 발송 로직 시작!");

        long startTime = System.nanoTime(); // 시작 시간 측정

        int pageNumber = 0;
        int pageSize = 500;
        Page<SeatNotificationSubscription> page;

        // 빈자리 알림 메세지 정보 (구독한 빈자리 알림)
        SeatNotification seatNotification = seatNotificationRepository.findBySeatId(seatId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOT_FOUND));

        NotificationType type = NotificationType.SEAT_NOTIFICATION;

        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            page = seatNotificationSubscriptionRepository.findAllBySeatId(seatId, pageable);
            List<SeatNotificationSubscription> seatNotificationSubscriptionList = page.getContent();

            if (seatNotificationSubscriptionList.isEmpty()) {
                log.info("❌ 구독자가 없습니다. 알림 발송을 중단합니다.");
                return;
            }

            // 알림 전송
            List<FcmSendingResultDto> resultList = handleMulticastNotification(seatNotification, seatNotificationSubscriptionList);

            log.info("✅ 빈자리 알림 발송 완료 (Batch {}): {} 개", pageNumber, seatNotificationSubscriptionList.size());

            // 전송된 알림 히스토리를 배치로 저장
            List<NotificationHistory> notificationHistoryList = buildMulticastNotificationHistory(seatNotificationSubscriptionList, seatNotification, type, resultList);
            saveNotificationHistory(notificationHistoryList);

            pageNumber++; // 다음 페이지로 이동
        } while (page.hasNext()); // 다음 페이지가 있으면 계속 반복

        long endTime = System.nanoTime(); // 종료 시간 측정
        long elapsedTime = endTime - startTime; // 경과 시간 (나노초 단위)
        log.info("sendSeatNotification 실행 시간: {} ms", elapsedTime / 1_000_000);
    }

    /**
     * multicastMessage를 처리하는 메서드
     *
     * @param seatNotification
     * @param seatNotificationSubscriptionList
     * @return
     */
    private List<FcmSendingResultDto> handleMulticastNotification(SeatNotification seatNotification, List<SeatNotificationSubscription> seatNotificationSubscriptionList) {
        MulticastMessage multicastMessage = buildSeatNotifications(seatNotification, seatNotificationSubscriptionList);

        // FCM 메세지 전송 및 전송 결과 반환
        return notificationSender.sendMulticastNotification(multicastMessage);
    }

    /**
     * 빈자리 알림 FCM 메세지릴 build하는 메서드
     *
     * @param seatNotification
     * @param seatNotificationSubscriptionList
     * @return
     */
    private MulticastMessage buildSeatNotifications(SeatNotification seatNotification, List<SeatNotificationSubscription> seatNotificationSubscriptionList) {
        List<String> tokenList = new ArrayList<>();
        for (SeatNotificationSubscription notification : seatNotificationSubscriptionList) {
            List<String> userTokens = fcmRepository.findByUser(notification.getUser().getId());
            tokenList.addAll(userTokens);
        }
        if (tokenList.isEmpty()) throw new CustomException(FcmErrorCode.NO_FCM_TOKEN_REGISTERED);

        // FCM 메시지 빌드 후 반환
        return createMulticastMessageWithUrl(seatNotification.getTitle(), seatNotification.getBody(), seatNotification.getUrl(), tokenList);
    }

    /**
     * Multicast Message의 알림 history 데이터를 build 하는 메서드
     *
     * @param seatNotificationSubscriptionList
     * @param seatNotification
     * @param type
     * @param resultList
     * @return
     */
    private List<NotificationHistory> buildMulticastNotificationHistory(List<SeatNotificationSubscription> seatNotificationSubscriptionList,
                                                                        SeatNotification seatNotification,
                                                                        NotificationType type, List<FcmSendingResultDto> resultList) {
        List<NotificationHistory> notificationHistoryList = new ArrayList<>();

        // 보낸 FCM 메세지를 JSON으로 저장하기 위해 Map 사용
        Map<String, String> messageHistory = new HashMap<>();
        messageHistory.put("title", seatNotification.getTitle());
        messageHistory.put("body", seatNotification.getBody());
        messageHistory.put("url", seatNotification.getUrl());

        for (int i = 0; i < seatNotificationSubscriptionList.size(); i++) {
            NotificationHistory notificationHistory = createNotificationHistory(type, messageHistory, resultList.get(i).getSentAt(),
                    resultList.get(i).getSentResult(), resultList.get(i).getErrorCode(), null, seatNotificationSubscriptionList.get(i).getUser());
            // fcm 토큰 저장 로직 구현 필요

            notificationHistoryList.add(notificationHistory);
        }
        // 알림 history 빌드

        return notificationHistoryList;
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
        List<SeatNotification> messages = seatNotificationRepository.findAllByCurrentDateTime(currentDate, currentTime);
        // 삭제(Cascade 설정으로 알림 신청 데이터도 삭제됨)
        seatNotificationRepository.deleteAll(messages);
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
     *
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
        String title = NotificationContentUtil.formatReservationCompleteNotificationTitle(restaurantName);
        String body = NotificationContentUtil.formatReservationCompleteNotificationBody(reservedDate, reservedTime, minCapacity,
                maxCapacity);
        String url = NotificationContentUtil.formatReservationNotificationUrl();

        return new FcmMessageDto(title, body, url);
    }

    /**
     * FCM 메세지 리스트를 build하는 내부 메서드
     *
     * @param user
     * @param fcmMessageDto
     * @return
     */
    private GroupFcmInformationDto buildMessageList(User user, FcmMessageDto fcmMessageDto) {
        List<Fcm> tokenList = user.getFcmList();
        if (tokenList.isEmpty()) throw new CustomException(FcmErrorCode.NO_FCM_TOKEN_REGISTERED);

        List<Message> messageList = new ArrayList<>();
        List<FcmMessageDto> fcmMessageDtoList = new ArrayList<>();
        Message message;

        for (Fcm token : tokenList) {
            message = createMessageWithUrl(fcmMessageDto.getTitle(), fcmMessageDto.getBody(), fcmMessageDto.getUrl(), token.getToken());
            messageList.add(message);

            fcmMessageDtoList.add(FcmMessageDto.of(fcmMessageDto, token.getToken(), user));
        }

        return new GroupFcmInformationDto(fcmMessageDtoList, messageList);
    }


    /**
     * 하나의 사용자를 대상으로 하는 단 건 알림을 발송하는 내부 메서드
     *
     * @param messageList
     * @param type
     * @param fcmMessageDtoList
     * @return
     */
    private List<NotificationHistory> sendSingleNotification(List<Message> messageList, NotificationType type, List<FcmMessageDto> fcmMessageDtoList) {
        // FCM 메세지 전송 및 전송 결과 반환
        List<FcmSendingResultDto> resultList = notificationSender.sendEachNotification(messageList);
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
     *
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
            title = formatReservationFullCanceledNotificationTitle(restaurantName);
            body = formatReservationFullCanceledNotificationBody(reservedDate, reservedTime, user.getNormalTicketCount());
        } else {
            title = formatReservationNullCanceledNotificationTitle(restaurantName);
            body = formatReservationNullCanceledNotificationBody(reservedDate, reservedTime, user.getNormalTicketCount());
        }

        String url = formatReservationNotificationUrl();

        return new FcmMessageDto(title, body, url);
    }
}
