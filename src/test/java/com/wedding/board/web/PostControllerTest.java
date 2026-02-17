package com.wedding.board.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.wedding.board.application.comment.CommentApplicationService;
import com.wedding.board.application.post.CreatePostCommand;
import com.wedding.board.application.post.PostApplicationService;
import com.wedding.board.application.post.UpdatePostCommand;
import com.wedding.board.domain.board.Board;
import com.wedding.board.domain.post.Post;
import com.wedding.board.domain.user.User;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)  // 컨트롤러 로직만 테스트 (보안 필터 비활성화)
@DisplayName("PostController")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostApplicationService postApplicationService;

    @MockBean
    private CommentApplicationService commentApplicationService;

    private final Board board = Board.of("GENERAL", "자유게시판");

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    @DisplayName("GET /boards/{boardCode}/posts: 게시글 목록을 조회한다")
    void list() throws Exception {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        Page<Post> page = new PageImpl<>(List.of(post));
        given(postApplicationService.getPosts(eq("GENERAL"), any(PageRequest.class))).willReturn(page);

        mockMvc.perform(get("/boards/GENERAL/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("boardCode", "GENERAL"));

        verify(postApplicationService).getPosts(eq("GENERAL"), any(PageRequest.class));
    }

    @Test
    @DisplayName("GET /boards/{boardCode}/posts/{id}: 게시글 상세와 댓글을 조회한다")
    void detail() throws Exception {
        User author = User.create("user1", "encoded");
        Post post = Post.create(board, "제목", "내용", author, null, null, null, null, null);
        given(postApplicationService.getPost(1L)).willReturn(post);
        given(commentApplicationService.getCommentsByPostId(1L)).willReturn(List.of());

        mockMvc.perform(get("/boards/GENERAL/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("boardCode", "GENERAL"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attributeExists("commentForm"));

        verify(postApplicationService).getPost(1L);
        verify(commentApplicationService).getCommentsByPostId(1L);
    }

    @Test
    @DisplayName("GET /boards/{boardCode}/posts/new: 글 작성 폼을 보여준다")
    void createForm() throws Exception {
        mockMvc.perform(get("/boards/GENERAL/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attribute("boardCode", "GENERAL"));
    }

    @Test
    @DisplayName("POST /boards/{boardCode}/posts: 게시글을 생성한다")
    void create() throws Exception {
        TestSecurityUtils.setMockUser(1L);
        given(postApplicationService.createPost(any(CreatePostCommand.class))).willReturn(1L);

        mockMvc.perform(post("/boards/GENERAL/posts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "새 제목")
                        .param("content", "새 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/boards/GENERAL/posts/1"));

        verify(postApplicationService).createPost(any(CreatePostCommand.class));
    }

    @Test
    @DisplayName("POST /boards/{boardCode}/posts: 유효성 검증 실패 시 폼으로 돌아간다")
    void create_validationError() throws Exception {
        TestSecurityUtils.setMockUser(1L);

        mockMvc.perform(post("/boards/GENERAL/posts")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasErrors("postForm"));
    }

    @Test
    @DisplayName("PUT /boards/{boardCode}/posts/{id}: 게시글을 수정한다")
    void update() throws Exception {
        mockMvc.perform(put("/boards/GENERAL/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"수정된 제목\",\"content\":\"수정된 내용\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.id").value(1));

        verify(postApplicationService).updatePost(eq(1L), any(UpdatePostCommand.class));
    }

    @Test
    @DisplayName("DELETE /boards/{boardCode}/posts/{id}: 게시글을 삭제한다")
    void deletePost() throws Exception {
        mockMvc.perform(delete("/boards/GENERAL/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(postApplicationService).deletePost(1L);
    }
}
