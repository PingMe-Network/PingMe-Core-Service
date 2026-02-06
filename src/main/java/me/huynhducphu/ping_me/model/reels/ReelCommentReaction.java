// model/reels/ReelCommentReaction.java
package me.huynhducphu.ping_me.model.reels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ReelCommentReaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private ReelComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    public ReelCommentReaction(ReelComment c, User u, ReactionType t) {
        this.comment = c;
        this.user = u;
        this.type = t;
    }
}
