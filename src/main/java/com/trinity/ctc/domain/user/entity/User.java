package com.trinity.ctc.domain.user.entity;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.like.entity.Likes;
import com.trinity.ctc.domain.notification.entity.Notification;
import com.trinity.ctc.domain.payment.entity.PaymentHistory;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.search.entity.SearchHistory;
import com.trinity.ctc.domain.user.status.Sex;
import com.trinity.ctc.domain.user.status.UserStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private String phoneNumber;
    private Integer normalTicketCount;
    private Integer emptyTicketCount;
    private Sex sex;
    private String imageUrl;
    private Boolean isDeleted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Fcm> fcmList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<SearchHistory> searchHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Notification> notificationList = new ArrayList<>();

    @OneToOne(mappedBy = "user")
    private UserPreference userPreference;

    @OneToMany(mappedBy = "user")
    private List<Likes> likeList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PaymentHistory> paymentHistoryList = new ArrayList();

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservationList = new ArrayList<>();

}
