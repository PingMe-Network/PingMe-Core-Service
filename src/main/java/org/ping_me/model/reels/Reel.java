package org.ping_me.model.reels;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.ping_me.model.User;
import org.ping_me.model.common.BaseEntity;
import org.ping_me.model.constant.ReelStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reels")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Reel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * =====================================
     * Nội dung chính
     * =====================================
     */

    @Column(nullable = false)
    String videoUrl;

    @Column(length = 200)
    String caption;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reel_hashtags", joinColumns = @JoinColumn(name = "reel_id"))
    @Column(name = "tag", length = 100)
    List<String> hashtags = new ArrayList<>();

    /**
     * =====================================
     * Trạng thái & Số liệu thống kê
     * =====================================
     */

    @Builder.Default
    @Column(nullable = false)
    Long viewCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ReelStatus status = ReelStatus.ACTIVE;

    @Column(length = 500)
    String adminNote;

    /**
     * =====================================
     * Người đăng reel này
     * =====================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    /**
     * =====================================
     * Quan hệ phụ thuộc
     * =====================================
     */

    @Builder.Default
    @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReelLike> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReelSave> saves = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReelView> views = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ReelComment> comments = new ArrayList<>();


    /**
     * =====================================
     * Constructor
     * =====================================
     */
    public Reel(String videoUrl, String caption, List<String> hashtags) {
        this.videoUrl = videoUrl;
        this.caption = caption;
        this.hashtags = hashtags != null ? hashtags : new ArrayList<>();
        this.viewCount = 0L;
    }


}
