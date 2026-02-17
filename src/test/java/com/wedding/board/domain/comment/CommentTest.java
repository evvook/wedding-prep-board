package com.wedding.board.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;

import com.wedding.board.domain.board.Board;
import com.wedding.board.domain.post.Post;
import com.wedding.board.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Comment 도메인")
class CommentTest {

    private final Board board = Board.of("GENERAL", "자유게시판");

    @Test
    @DisplayName("create: 내용, 게시글, 작성자로 댓글을 생성한다")
    void create() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Comment comment = Comment.create("댓글 내용", post, author);

        assertThat(comment.getContent()).isEqualTo("댓글 내용");
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getCreatedAt()).isNotNull();
    }
}
