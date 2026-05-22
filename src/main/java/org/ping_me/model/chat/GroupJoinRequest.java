package org.ping_me.model.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ping_me.model.User;
import org.ping_me.model.common.BaseEntity;
import org.ping_me.model.constant.GroupJoinRequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_join_requests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_join_request_room_user", columnNames = {"room_id", "requester_id"})
        },
        indexes = {
                @Index(name = "idx_group_join_request_room_status", columnList = "room_id, status")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GroupJoinRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupJoinRequestStatus status = GroupJoinRequestStatus.PENDING;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
