package org.example.tnal_youth_backend.authentication.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;

import java.time.OffsetDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "otp_code_hash", nullable = false)
    private String otpCodeHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel", nullable = false, length = 20)
    private OtpChannel deliveryChannel;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }

        if (attempts == null) {
            attempts = 0;
        }
    }
}