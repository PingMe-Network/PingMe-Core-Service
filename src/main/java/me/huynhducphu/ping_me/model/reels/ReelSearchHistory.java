package me.huynhducphu.ping_me.model.reels;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;

@Entity
@Table(name = "reel_search_histories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ReelSearchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    String query;

    Integer resultCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
}

