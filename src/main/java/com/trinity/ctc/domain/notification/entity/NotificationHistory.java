package com.trinity.ctc.domain.notification.entity;

import com.trinity.ctc.domain.notification.entity.type.NotificationType;
import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class NotificationHistory {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime sendingAt;

    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User user;
}
