package org.ping_me.repository.jpa.music;

import org.ping_me.model.music.SongArtistRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Le Tran Gia Huy
 * @created 20/11/2025 - 6:23 PM
 * @project DHKTPM18ATT_Nhom10_PingMe_Backend
 * @package me.huynhducphu.PingMe_Backend.repository
 */

@Repository
public interface SongArtistRoleRepository extends JpaRepository<SongArtistRole, Long> {
    List<SongArtistRole> findSongArtistRolesByArtist_Id(Long artistId);
}
