package com.trinity.ctc.domain.fcm.entity;

import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fcm {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Date registeredAt;
    private Date updatedAt;
    private Date expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User user;

    @Builder
    public Fcm(String token, Date registeredAt, Date expiresAt, User user){
        this.token = token;
        this.registeredAt = registeredAt;
        this.expiresAt = expiresAt;
        this.user = user;
    }
}
