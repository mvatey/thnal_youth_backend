package org.example.tnal_youth_backend.model.entity;




import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private UUID token;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    private Boolean revoked;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}