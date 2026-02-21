package org.ping_me.repository.jpa.music;

import org.ping_me.dto.response.music.misc.TopSongPlayCounterDto;
import org.ping_me.model.music.SongPlayHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SongPlayHistoryRepository extends JpaRepository<SongPlayHistory, Long> {
    @Query("""
                SELECT  new  org.ping_me.dto.response.music.misc.TopSongPlayCounterDto(
                    s.id, s.title, s.imgUrl, COUNT(h.id)
                )
                FROM SongPlayHistory h
                JOIN h.song s
                WHERE h.playedAt BETWEEN :start AND :end
                GROUP BY s.id, s.title, s.imgUrl
                ORDER BY COUNT(h.id) DESC
            """)
    List<TopSongPlayCounterDto> findTopSongsWithPlayCount(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}
