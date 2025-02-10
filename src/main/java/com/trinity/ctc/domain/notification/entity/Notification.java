package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Notification {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Boolean isRead;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private LocalDateTime createdAt;
    private LocalDateTime sendingAt;

    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User user;
}
