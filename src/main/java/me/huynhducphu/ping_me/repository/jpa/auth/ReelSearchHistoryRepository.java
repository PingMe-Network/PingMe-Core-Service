package me.huynhducphu.ping_me.repository.jpa.auth;

import me.huynhducphu.ping_me.model.reels.ReelSearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReelSearchHistoryRepository extends JpaRepository<ReelSearchHistory, Long> {
    List<ReelSearchHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    Page<ReelSearchHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("delete from ReelSearchHistory r where r.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}

