package com.trinity.ctc.domain.user.entity;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.payment.entity.PaymentHistory;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.search.entity.SearchHistory;
import com.trinity.ctc.domain.user.dto.OnboardingRequest;
import com.trinity.ctc.domain.user.status.Sex;
import com.trinity.ctc.domain.user.status.UserStatus;
import com.trinity.ctc.util.validator.TicketValidator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long kakaoId;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private String phoneNumber;
    private Integer normalTicketCount;
    private Integer emptyTicketCount;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    private LocalDate birthday;
    private String imageUrl;
    private Boolean isDeleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Fcm> fcmList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SearchHistory> searchHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<NotificationHistory> notificationList = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserPreference userPreference;

    @OneToMany(mappedBy = "user")
    private List<Likes> likeList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PaymentHistory> paymentHistoryList = new ArrayList();

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservationList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationNotification> reservationNotificationList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatNotification> seatNotificationList = new ArrayList<>();

    @Builder
    public User(Long kakaoId, Integer normalTicketCount, Integer emptyTicket, UserStatus status) {
        this.kakaoId = kakaoId;
        this.normalTicketCount = normalTicketCount;
        this.emptyTicketCount = emptyTicket;
        this.status = status;
    }

    /* 내부 메서드 */
    public void returnNormalTickets() {
        this.normalTicketCount += 10;
    }

    public void payNormalTickets() {
        TicketValidator.validateTicketCount(this.normalTicketCount, 10);
        this.normalTicketCount -= 10;
    }

    public void useEmptyTicket() {
        this.emptyTicketCount--;
    }

    public void updateOnboardingInformation(OnboardingRequest onboardingRequest, UserPreference userPreference) {
        this.sex = onboardingRequest.getSex();
        this.birthday = onboardingRequest.getBirthday();
        this.phoneNumber = onboardingRequest.getPhoneNumber();
        this.userPreference = userPreference;
        this.status = UserStatus.AVAILABLE;
    }
}
