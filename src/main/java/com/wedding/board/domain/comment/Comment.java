package com.wedding.board.domain.comment;

import com.wedding.board.domain.post.Post;
import com.wedding.board.domain.user.User;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private LocalDateTime createdAt;

    private boolean deleted = false;

    @Transient
    private List<Comment> replies = new ArrayList<>();

    private Comment(String content, Post post, User author, Comment parent) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
    }

    public static Comment create(String content, Post post, User author) {
        return new Comment(content, post, author, null);
    }

    public static Comment createReply(String content, Post post, User author, Comment parent) {
        return new Comment(content, post, author, parent);
    }

    public void update(String content) {
        this.content = content;
    }

    public boolean isWrittenBy(Long userId) {
        return author.getId().equals(userId);
    }

    public void markAsDeleted() {
        this.deleted = true;
    }
}
