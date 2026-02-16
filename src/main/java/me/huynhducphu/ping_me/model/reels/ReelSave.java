package me.huynhducphu.ping_me.model.reels;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.huynhducphu.ping_me.model.User;
import me.huynhducphu.ping_me.model.common.BaseEntity;

@Entity
@Table(
        name = "reel_saves",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reel_id", "user_id"})
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReelSave extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reel_id", nullable = false)
    Reel reel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    public ReelSave(Reel reel, User user) {
        this.reel = reel;
        this.user = user;
    }
}

