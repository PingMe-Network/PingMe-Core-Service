package org.ping_me.repository.jpa.music;

import org.ping_me.model.music.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    Optional<PlaylistSong> findByPlaylistIdAndSongId(Long playlistId, Long songId);

    void deleteByPlaylistIdAndSongId(Long playlistId, Long songId);

    List<PlaylistSong> findByPlaylistIdOrderByPositionAsc(Long playlistId);
}
