package com.wedding.board.application.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.wedding.board.domain.board.Board;
import com.wedding.board.domain.board.BoardRepository;
import com.wedding.board.domain.comment.CommentRepository;
import com.wedding.board.domain.post.Post;
import com.wedding.board.domain.post.PostRepository;
import com.wedding.board.domain.user.User;
import com.wedding.board.domain.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostApplicationService")
class PostApplicationServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostApplicationService postApplicationService;

    private final Board board = Board.of("GENERAL", "자유게시판");

    @Test
    @DisplayName("getPosts: 작성일 역순으로 페이징된 게시글 목록을 반환한다")
    void getPosts() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Page<Post> page = new PageImpl<>(java.util.List.of(post));
        given(postRepository.findByBoardCodeIncludingLegacy("GENERAL", any())).willReturn(page);

        Page<Post> result = postApplicationService.getPosts("GENERAL", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("제목");
        verify(postRepository).findByBoardCodeIncludingLegacy("GENERAL", any(Pageable.class));
    }

    @Test
    @DisplayName("getPost: 존재하는 게시글을 조회한다")
    void getPost() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Post result = postApplicationService.getPost(1L);

        assertThat(result.getTitle()).isEqualTo("제목");
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("getPost: 존재하지 않는 게시글 조회 시 예외를 던진다")
    void getPost_notFound() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postApplicationService.getPost(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("createPost: 게시글을 생성하고 id를 반환한다")
    void createPost() {
        given(boardRepository.findById("GENERAL")).willReturn(Optional.of(board));
        User savedUser = User.create("user1", "encoded");
        given(userRepository.findById(1L)).willReturn(Optional.of(savedUser));

        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 1L);
            return p;
        });

        CreatePostCommand command = new CreatePostCommand("GENERAL", "제목", "내용", 1L, null, null, null, null, null);
        Long postId = postApplicationService.createPost(command);

        verify(postRepository).save(any(Post.class));
        assertThat(postId).isEqualTo(1L);
    }

    @Test
    @DisplayName("createPost: 존재하지 않는 사용자로 생성 시 예외를 던진다")
    void createPost_userNotFound() {
        given(boardRepository.findById("GENERAL")).willReturn(Optional.of(board));
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        CreatePostCommand command = new CreatePostCommand("GENERAL", "제목", "내용", 999L, null, null, null, null, null);

        assertThatThrownBy(() -> postApplicationService.createPost(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("updatePost: 게시글을 수정한다")
    void updatePost() {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        UpdatePostCommand command = new UpdatePostCommand("수정 제목", "수정 내용", null, null, null, null, null);
        postApplicationService.updatePost(1L, command);

        assertThat(post.getTitle()).isEqualTo("수정 제목");
        assertThat(post.getContent()).isEqualTo("수정 내용");
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("deletePost: 게시글을 삭제한다")
    void deletePost() {
        postApplicationService.deletePost(1L);
        verify(postRepository).deleteById(1L);
    }
}
