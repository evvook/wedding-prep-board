package com.wedding.board.domain.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedding.board.domain.board.Board;
import com.wedding.board.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Post 도메인")
class PostTest {

    private final Board board = Board.of("GENERAL", "자유게시판");

    @Test
    @DisplayName("create: 제목, 내용, 작성자로 게시글을 생성하고 createdAt과 updatedAt이 동일하다")
    void create() {
        User author = User.create("user1", "encodedPassword");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);

        assertThat(post.getTitle()).isEqualTo("제목");
        assertThat(post.getContent()).isEqualTo("내용");
        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getCreatedAt()).isNotNull();
        assertThat(post.getUpdatedAt()).isNotNull();
        assertThat(post.getCreatedAt()).isEqualTo(post.getUpdatedAt());
    }

    @Test
    @DisplayName("update: 제목과 내용을 수정하고 updatedAt이 갱신된다")
    void update() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "원래 제목", "원래 내용", author, null, null, null, null, null);
        var createdAt = post.getCreatedAt();

        post.update("수정된 제목", "수정된 내용", null, null, null, null, null);

        assertThat(post.getTitle()).isEqualTo("수정된 제목");
        assertThat(post.getContent()).isEqualTo("수정된 내용");
        assertThat(post.getUpdatedAt()).isAfterOrEqualTo(createdAt);
    }
}
