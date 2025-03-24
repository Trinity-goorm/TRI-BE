package com.trinity.ctc.domain.user.entity;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.notification.entity.NotificationHistory;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.payment.entity.PaymentHistory;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.search.entity.SearchHistory;
import com.trinity.ctc.domain.user.dto.OnboardingRequest;
import com.trinity.ctc.domain.user.status.Sex;
import com.trinity.ctc.domain.user.status.UserStatus;
import com.trinity.ctc.domain.user.validator.NormalTicketValidator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
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
    private List<SeatNotificationSubscription> seatNotificationSubscriptionList = new ArrayList<>();

    @Builder
    public User(Long kakaoId, Integer normalTicketCount, Integer emptyTicket, UserStatus status, Sex sex, String imageUrl, String nickname, String phoneNumber, LocalDate birthday, boolean isDeleted) {
        this.kakaoId = kakaoId;
        this.normalTicketCount = normalTicketCount;
        this.emptyTicketCount = emptyTicket;
        this.status = status;
        this.sex = sex;
        this.imageUrl = imageUrl;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.birthday = birthday;
        this.isDeleted = isDeleted;
    }

    /* 내부 메서드 */
    public void returnNormalTickets() {
        this.normalTicketCount += 10;
    }

    public void payNormalTickets() {
        NormalTicketValidator.validateTicketCount(this.normalTicketCount, 10);
        this.normalTicketCount -= 10;
    }

    public void useEmptyTicket() {
        this.emptyTicketCount--;
    }

    public void updateOnboardingInformation(OnboardingRequest onboardingRequest, UserPreference userPreference) {
        this.nickname = onboardingRequest.getName();
        this.sex = onboardingRequest.getSex();
        this.birthday = onboardingRequest.getBirthday();
        this.phoneNumber = onboardingRequest.getPhoneNumber();
        this.userPreference = userPreference;
        this.status = UserStatus.AVAILABLE;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + status.name()));
    }
}
