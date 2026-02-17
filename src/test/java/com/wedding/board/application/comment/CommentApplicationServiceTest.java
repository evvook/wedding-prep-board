package com.wedding.board.application.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.wedding.board.domain.board.Board;
import com.wedding.board.domain.comment.Comment;
import com.wedding.board.domain.comment.CommentRepository;
import com.wedding.board.domain.post.Post;
import com.wedding.board.domain.post.PostRepository;
import com.wedding.board.domain.user.User;
import com.wedding.board.domain.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentApplicationService")
class CommentApplicationServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentApplicationService commentApplicationService;

    private final Board board = Board.of("GENERAL", "자유게시판");

    @Test
    @DisplayName("getCommentsByPostId: 게시글의 댓글 목록을 작성일 순으로 반환한다")
    void getCommentsByPostId() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Comment comment = Comment.create("댓글", post, author);
        given(commentRepository.findByPostIdOrderByCreatedAtAsc(1L)).willReturn(List.of(comment));

        List<Comment> result = commentApplicationService.getCommentsByPostId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("댓글");
    }

    @Test
    @DisplayName("createComment: 댓글을 생성하고 id를 반환한다")
    void createComment() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(1L)).willReturn(Optional.of(author));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 1L);
            return c;
        });

        CreateCommentCommand command = new CreateCommentCommand("댓글 내용", 1L, 1L, null);
        Long commentId = commentApplicationService.createComment(command);

        verify(commentRepository).save(any(Comment.class));
        assertThat(commentId).isEqualTo(1L);
    }

    @Test
    @DisplayName("createComment: 존재하지 않는 게시글에 댓글 작성 시 예외를 던진다")
    void createComment_postNotFound() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());
        CreateCommentCommand command = new CreateCommentCommand("댓글", 999L, 1L, null);

        assertThatThrownBy(() -> commentApplicationService.createComment(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("createComment: 대댓글을 생성한다")
    void createComment_reply() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        ReflectionTestUtils.setField(post, "id", 1L);
        Comment parent = Comment.create("부모 댓글", post, author);
        ReflectionTestUtils.setField(parent, "id", 1L);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(1L)).willReturn(Optional.of(author));
        given(commentRepository.findById(1L)).willReturn(Optional.of(parent));
        given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 2L);
            return c;
        });

        CreateCommentCommand command = new CreateCommentCommand("대댓글 내용", 1L, 1L, 1L);
        Long commentId = commentApplicationService.createComment(command);

        verify(commentRepository).save(any(Comment.class));
        assertThat(commentId).isEqualTo(2L);
    }

    @Test
    @DisplayName("updateComment: 댓글을 수정한다")
    void updateComment() {
        User author = User.create("user1", "encoded");
        ReflectionTestUtils.setField(author, "id", 1L);
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Comment comment = Comment.create("댓글", post, author);
        ReflectionTestUtils.setField(comment, "id", 1L);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        commentApplicationService.updateComment(1L, new UpdateCommentCommand("수정된 내용"), 1L);

        assertThat(comment.getContent()).isEqualTo("수정된 내용");
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("updateComment: 본인 댓글이 아니면 예외를 던진다")
    void updateComment_notAuthor() {
        User author = User.create("user1", "encoded");
        ReflectionTestUtils.setField(author, "id", 1L);
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Comment comment = Comment.create("댓글", post, author);
        ReflectionTestUtils.setField(comment, "id", 1L);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentApplicationService.updateComment(1L, new UpdateCommentCommand("수정"), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("deleteComment: 댓글을 삭제한다")
    void deleteComment() {
        User author = User.create("user1", "encoded");
        ReflectionTestUtils.setField(author, "id", 1L);
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Comment comment = Comment.create("댓글", post, author);
        ReflectionTestUtils.setField(comment, "id", 1L);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        commentApplicationService.deleteComment(1L, 1L);

        assertThat(comment.isDeleted()).isTrue();
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("deleteComment: 본인 댓글이 아니면 예외를 던진다")
    void deleteComment_notAuthor() {
        User author = User.create("user1", "encoded");
        ReflectionTestUtils.setField(author, "id", 1L);
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Comment comment = Comment.create("댓글", post, author);
        ReflectionTestUtils.setField(comment, "id", 1L);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentApplicationService.deleteComment(1L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인의 댓글만 삭제할 수 있습니다");
    }
}
