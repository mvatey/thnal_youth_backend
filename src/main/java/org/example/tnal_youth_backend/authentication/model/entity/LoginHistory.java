package org.example.tnal_youth_backend.authentication.model.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "login_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "login_time")
    private OffsetDateTime loginTime;

    @Column(name = "ip_address")
    private String ipAddress;

    private String device;

    private String browser;

    private Boolean success;
}