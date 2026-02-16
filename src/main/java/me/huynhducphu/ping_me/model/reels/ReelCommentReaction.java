// model/reels/ReelCommentReaction.java
package me.huynhducphu.ping_me.model.reels;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import me.huynhducphu.ping_me.model.constant.ReactionType;

@Entity
@Table(
        name = "reel_comment_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"})
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ReelCommentReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    ReelComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ReactionType type;

    public ReelCommentReaction(ReelComment c, User u, ReactionType t) {
        this.comment = c;
        this.user = u;
        this.type = t;
    }
}
