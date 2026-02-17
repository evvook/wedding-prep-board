package com.wedding.board.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByBoard_CodeOrderByCreatedAtDesc(String boardCode, Pageable pageable);

    /** GENERAL: board=GENERAL 또는 board=null(기존 글) 포함 */
    @Query("SELECT p FROM Post p WHERE p.board.code = :boardCode OR (p.board IS NULL AND :boardCode = 'GENERAL') ORDER BY p.createdAt DESC")
    Page<Post> findByBoardCodeIncludingLegacy(@Param("boardCode") String boardCode, Pageable pageable);
}
