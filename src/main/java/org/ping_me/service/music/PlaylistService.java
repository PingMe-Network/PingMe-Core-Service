package org.ping_me.service.music;

import org.ping_me.dto.response.music.misc.PlaylistDetailDto;
import org.ping_me.dto.response.music.misc.PlaylistDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PlaylistService {
    PlaylistDto createPlaylist(PlaylistDto dto);

    List<PlaylistDto> getPlaylistsByUser();

    PlaylistDetailDto getPlaylistDetail(Long playlistId);

    void deletePlaylist(Long playlistId);

    Page<PlaylistDto> getPublicPlaylists(int page, int size);

    boolean addSongToPlaylist(Long playlistId, Long songId);

    void removeSongFromPlaylist(Long playlistId, Long songId);

    PlaylistDto updatePlaylist(Long playlistId, PlaylistDto dto);

    void reorderPlaylist(Long playlistId, List<Long> orderedSongIds);
}
