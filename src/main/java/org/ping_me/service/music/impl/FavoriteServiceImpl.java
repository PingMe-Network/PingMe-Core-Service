package org.ping_me.service.music.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.ping_me.dto.response.music.misc.FavoriteDto;
import org.ping_me.model.music.FavoriteSong;
import org.ping_me.repository.jpa.auth.UserRepository;
import org.ping_me.repository.jpa.music.FavoriteSongRepository;
import org.ping_me.repository.jpa.music.SongRepository;
import org.ping_me.service.music.FavoriteService;
import org.ping_me.service.user.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteServiceImpl implements FavoriteService {

    // Repository
    FavoriteSongRepository favoriteSongRepository;
    SongRepository songRepository;
    UserRepository userRepository;

    // Provider
    CurrentUserProvider currentUserProvider;
    
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
