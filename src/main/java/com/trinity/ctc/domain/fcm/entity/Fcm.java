package com.trinity.ctc.domain.fcm.entity;

import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Fcm {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Date createdAt;
    private Date updatedAt;
    private Date expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User user;

}
