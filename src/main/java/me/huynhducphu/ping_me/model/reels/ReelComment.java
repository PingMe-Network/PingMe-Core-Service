package me.huynhducphu.ping_me.model.reels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;

@Entity
@Table(name = "reel_comments")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class ReelComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reel_id", nullable = false)
    private Reel reel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ReelComment parent;

    @Column(nullable = false)
    private Boolean isPinned = false;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ReelComment> replies = new java.util.ArrayList<>();

    public ReelComment(String content, Reel reel, User user) {
        this.content = content;
        this.reel = reel;
        this.user = user;
    }
}
