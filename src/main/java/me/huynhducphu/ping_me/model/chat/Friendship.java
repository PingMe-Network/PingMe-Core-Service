package me.huynhducphu.ping_me.model.chat;

import jakarta.persistence.*;
import lombok.*;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import me.huynhducphu.ping_me.model.constant.FriendshipStatus;
import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;

/**
 * Admin 8/19/2025
 **/
@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_friend_pair", columnNames = {"user_low_id", "user_high_id"})
        },
        indexes = {
                @Index(name = "idx_friend_status_usera", columnList = "status, user_a_id"),
                @Index(name = "idx_friend_status_userb", columnList = "status, user_b_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Friendship extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendshipStatus friendshipStatus;

    @Column(name = "user_low_id", nullable = false, updatable = false)
    private Long userLowId;

    @Column(name = "user_high_id", nullable = false, updatable = false)
    private Long userHighId;

    @PrePersist
    @PreUpdate
    private void normalizePair() {
        if (userA == null || userB == null) return;
        Long a = userA.getId();
        Long b = userB.getId();

        if (Objects.equals(a, b))
            throw new AccessDeniedException("Không thể kết bạn với chính mình");
        else if (a < b) {
            userLowId = a;
            userHighId = b;
        } else {
            userLowId = b;
            userHighId = a;
        }
    }
}
