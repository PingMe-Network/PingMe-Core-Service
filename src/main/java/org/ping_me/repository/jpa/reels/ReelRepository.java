package org.ping_me.repository.jpa.reels;

import org.ping_me.model.reels.Reel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReelRepository extends JpaRepository<Reel, Long>,
        JpaSpecificationExecutor<Reel> {

    Page<Reel> findAllByOrderByCreatedAtDesc(Pageable pageable);


    // Find reels created by specific user
    Page<Reel> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // search by caption (title) containing text, case-insensitive using native SQL
    @Query(
            value = """
                      SELECT * FROM reels r
                      WHERE LOWER(r.caption) LIKE LOWER(CONCAT('%', :q, '%'))
                      ORDER BY r.created_at DESC
                    """,
            nativeQuery = true
    )
    Page<Reel> searchByTitle(@Param("q") String q, Pageable pageable);

    // search by hashtag using the element collection table `reel_hashtags`.
    @Query(value = """
              SELECT r.* FROM reels r
              JOIN reel_hashtags h ON h.reel_id = r.id
              WHERE LOWER(h.tag) = LOWER(:tag)
              ORDER BY r.created_at DESC
            """, nativeQuery = true)
    Page<Reel> searchByHashtag(@Param("tag") String tag, Pageable pageable);
}
