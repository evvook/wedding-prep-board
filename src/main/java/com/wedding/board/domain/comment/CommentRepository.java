package com.wedding.board.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);

    void deleteByPostId(Long postId);

    void deleteByParent(Comment parent);

    void deleteByPostIdAndParentIsNotNull(Long postId);

    void deleteByPostIdAndParentIsNull(Long postId);
}
