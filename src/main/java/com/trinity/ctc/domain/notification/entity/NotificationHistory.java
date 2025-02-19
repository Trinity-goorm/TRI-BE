package com.trinity.ctc.domain.notification.entity;

import com.google.firebase.messaging.MessagingErrorCode;
import com.trinity.ctc.domain.notification.entity.result.SentResult;
import com.trinity.ctc.domain.notification.entity.type.NotificationType;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.util.formatter.JsonConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationHistory {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Convert(converter = JsonConverter.class)
    private Map<String, String> message;

    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    private SentResult sentResult;

    // FCM의 MessagingErrorCode enum -> FirebaseMessagingException에서 Nullable parameter로 처리
    @Enumerated(EnumType.STRING)
    private MessagingErrorCode errorCode;

    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User user;

    @Builder
    public NotificationHistory(NotificationType type, Map<String, String> message, LocalDateTime sentAt, SentResult sentResult,
                               @Nullable MessagingErrorCode errorCode, User user) {
        this.type = type;
        this.message = message;
        this.sentAt = sentAt;
        this.sentResult = sentResult;
        this.errorCode = errorCode;
        this.user = user;
    }
}
