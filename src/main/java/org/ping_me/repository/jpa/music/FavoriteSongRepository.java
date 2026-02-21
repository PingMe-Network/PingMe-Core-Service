package org.ping_me.repository.jpa.music;

import org.ping_me.model.music.FavoriteSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteSongRepository extends JpaRepository<FavoriteSong, Long> {
    List<FavoriteSong> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FavoriteSong> findByUserIdAndSongId(Long userId, Long songId);

    void deleteByUserIdAndSongId(Long userId, Long songId);
}
