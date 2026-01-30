package me.huynhducphu.ping_me.repository.jpa.music;

import me.huynhducphu.ping_me.model.music.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(Long userId);

    Page<Playlist> findByIsPublicTrue(Pageable pageable);
}
