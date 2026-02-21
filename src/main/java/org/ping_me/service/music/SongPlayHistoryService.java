package org.ping_me.service.music;

import org.ping_me.dto.response.music.misc.TopSongPlayCounterDto;

import java.util.List;

public interface SongPlayHistoryService {
    @SuppressWarnings("unchecked")
    List<TopSongPlayCounterDto> getTopSongsTodayCached(int limit);

    @SuppressWarnings("unchecked")
    List<TopSongPlayCounterDto> getTopSongsThisWeekCached(int limit);

    @SuppressWarnings("unchecked")
    List<TopSongPlayCounterDto> getTopSongsThisMonthCached(int limit);
}
