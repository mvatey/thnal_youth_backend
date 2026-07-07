package org.example.tnal_youth_backend.authentication.model.entity;





import org.example.tnal_youth_backend.authentication.model.enums.OtpChannel;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "otp_code_hash")
    private String otpCodeHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_channel")
    private OtpChannel deliveryChannel;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    private Integer attempts;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}