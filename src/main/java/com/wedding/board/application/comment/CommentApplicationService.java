package com.wedding.board.application.comment;

import com.wedding.board.domain.comment.Comment;
import com.wedding.board.domain.comment.CommentRepository;
import com.wedding.board.domain.post.Post;
import com.wedding.board.domain.post.PostRepository;
import com.wedding.board.domain.user.User;
import com.wedding.board.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentApplicationService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<Comment> getCommentsByPostId(Long postId) {
        List<Comment> all = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        Map<Long, List<Comment>> childrenMap = new HashMap<>();
        for (Comment c : all) {
            if (c.getParent() != null) {
                childrenMap.computeIfAbsent(c.getParent().getId(), k -> new ArrayList<>()).add(c);
            }
        }
        childrenMap.values().forEach(list -> list.sort(Comparator.comparing(Comment::getCreatedAt)));
        List<Comment> roots = all.stream()
                .filter(c -> c.getParent() == null)
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .collect(Collectors.toList());
        roots.forEach(root -> populateReplies(root, childrenMap));
        return roots;
    }

    private void populateReplies(Comment comment, Map<Long, List<Comment>> childrenMap) {
        List<Comment> replies = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        comment.getReplies().clear();
        comment.getReplies().addAll(replies);
        replies.forEach(reply -> populateReplies(reply, childrenMap));
    }

    public Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
    }

    @Transactional
    public Long createComment(CreateCommentCommand command) {
        Post post = postRepository.findById(command.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + command.getPostId()));
        User author = userRepository.findById(command.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Comment comment;
        if (command.getParentId() != null) {
            Comment parent = getComment(command.getParentId());
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException("대상 댓글이 해당 게시글에 속하지 않습니다");
            }
            if (parent.isDeleted()) {
                throw new IllegalArgumentException("삭제된 댓글에는 답글을 작성할 수 없습니다");
            }
            comment = Comment.createReply(command.getContent(), post, author, parent);
        } else {
            comment = Comment.create(command.getContent(), post, author);
        }
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public void updateComment(Long commentId, UpdateCommentCommand command, Long userId) {
        Comment comment = getComment(commentId);
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다");
        }
        if (!comment.isWrittenBy(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 수정할 수 있습니다");
        }
        comment.update(command.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getComment(commentId);
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다");
        }
        if (!comment.isWrittenBy(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다");
        }
        comment.markAsDeleted();
        commentRepository.save(comment);
    }
}
