package org.ping_me.service.music.impl;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.ping_me.dto.response.music.misc.TopSongPlayCounterDto;
import org.ping_me.repository.jpa.music.SongPlayHistoryRepository;
import org.ping_me.service.music.SongPlayHistoryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SongPlayHistoryServiceImpl implements SongPlayHistoryService {
    // Repository
    SongPlayHistoryRepository songPlayHistoryRepository;

    // Redis
    RedisTemplate<String, Object> redis;

    public SongPlayHistoryServiceImpl(
            SongPlayHistoryRepository songPlayHistoryRepository,
            @Qualifier("redisSongHistoryTemplate") RedisTemplate<String, Object> redis) {
        this.songPlayHistoryRepository = songPlayHistoryRepository;
        this.redis = redis;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TopSongPlayCounterDto> getTopSongsTodayCached(int limit) {
        String redisKey = "top:songs:today:" + limit;

        List<TopSongPlayCounterDto> cached = (List<TopSongPlayCounterDto>) redis.opsForValue().get(redisKey);
        if (cached != null) return cached;

        List<TopSongPlayCounterDto> topSongs = getTopSongsToday(limit);
        redis.opsForValue().set(redisKey, topSongs, Duration.ofMinutes(5));

        return topSongs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TopSongPlayCounterDto> getTopSongsThisWeekCached(int limit) {
        String redisKey = "top:songs:week:" + limit;
        List<TopSongPlayCounterDto> cached = (List<TopSongPlayCounterDto>) redis.opsForValue().get(redisKey);
        if (cached != null) return cached;

        List<TopSongPlayCounterDto> topSongs = getTopSongsThisWeek(limit);
        redis.opsForValue().set(redisKey, topSongs, Duration.ofMinutes(10));
        return topSongs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TopSongPlayCounterDto> getTopSongsThisMonthCached(int limit) {
        String redisKey = "top:songs:month:" + limit;
        List<TopSongPlayCounterDto> cached = (List<TopSongPlayCounterDto>) redis.opsForValue().get(redisKey);
        if (cached != null) return cached;

        List<TopSongPlayCounterDto> topSongs = getTopSongsThisMonth(limit);
        redis.opsForValue().set(redisKey, topSongs, Duration.ofMinutes(10));
        return topSongs;
    }

    public List<TopSongPlayCounterDto> getTopSongsToday(int limit) {
        return songPlayHistoryRepository.findTopSongsWithPlayCount(
                LocalDate.now().atStartOfDay(),
                LocalDateTime.now(),
                PageRequest.of(0, limit)
        );
    }

    public List<TopSongPlayCounterDto> getTopSongsThisWeek(int limit) {
        return songPlayHistoryRepository.findTopSongsWithPlayCount(
                LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay(),
                LocalDateTime.now(),
                PageRequest.of(0, limit)
        );
    }

    public List<TopSongPlayCounterDto> getTopSongsThisMonth(int limit) {
        return songPlayHistoryRepository.findTopSongsWithPlayCount(
                LocalDate.now().withDayOfMonth(1).atStartOfDay(),
                LocalDateTime.now(),
                PageRequest.of(0, limit)
        );
    }
}
