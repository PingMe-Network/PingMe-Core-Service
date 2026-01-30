package me.huynhducphu.ping_me.service.music.impl;

import lombok.RequiredArgsConstructor;
import me.huynhducphu.ping_me.dto.response.music.misc.FavoriteDto;
import me.huynhducphu.ping_me.model.music.FavoriteSong;
import me.huynhducphu.ping_me.repository.jpa.auth.UserRepository;
import me.huynhducphu.ping_me.repository.jpa.music.FavoriteSongRepository;
import me.huynhducphu.ping_me.repository.jpa.music.SongRepository;
import me.huynhducphu.ping_me.service.music.FavoriteService;
import me.huynhducphu.ping_me.service.user.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteSongRepository favoriteSongRepository;
    private final SongRepository songRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;

    @Override
    public List<FavoriteDto> getFavorites() {
        var userId = currentUserProvider.get().getId();
        return favoriteSongRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FavoriteDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public void addFavorite(Long songId) {
        var userId = currentUserProvider.get().getId();
        if (favoriteSongRepository.findByUserIdAndSongId(userId, songId).isPresent()) return;

        var user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        var song = songRepository.findById(songId).orElseThrow(() -> new RuntimeException("Song not found"));

        FavoriteSong fs = new FavoriteSong();
        fs.setUser(user);
        fs.setSong(song);
        favoriteSongRepository.save(fs);
    }


    @Override
    @Transactional
    public void removeFavorite(Long songId) {
        var userId = currentUserProvider.get().getId();
        favoriteSongRepository.deleteByUserIdAndSongId(userId, songId);
    }


    @Override
    public boolean isFavorite(Long songId) {
        var userId = currentUserProvider.get().getId();
        return favoriteSongRepository.findByUserIdAndSongId(userId, songId).isPresent();
    }

}
