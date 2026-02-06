package me.huynhducphu.ping_me.model.reels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;
import me.huynhducphu.ping_me.model.constant.ReelStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reels")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Reel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String videoUrl;

    @Column(length = 200)
    private String caption;

    // hashtags stored as a collection of individual tags (normalized, without leading '#')
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reel_hashtags", joinColumns = @JoinColumn(name = "reel_id"))
    @Column(name = "tag", length = 100)
    private List<String> hashtags = new ArrayList<>();

    @Column(nullable = false)
    private Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReelStatus status = ReelStatus.ACTIVE;

    @Column(length = 500)
    private String adminNote;

    public Reel(String videoUrl, String caption) {
        this.videoUrl = videoUrl;
        this.caption = caption;
        this.viewCount = 0L;
    }

    public Reel(String videoUrl, String caption, List<String> hashtags) {
        this.videoUrl = videoUrl;
        this.caption = caption;
        this.hashtags = hashtags != null ? hashtags : new ArrayList<>();
        this.viewCount = 0L;
    }
}
