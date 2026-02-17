package com.wedding.board.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.wedding.board.application.comment.CommentApplicationService;
import com.wedding.board.application.comment.CreateCommentCommand;
import com.wedding.board.application.post.PostApplicationService;
import com.wedding.board.domain.comment.Comment;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CommentController")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentApplicationService commentApplicationService;

    @MockBean
    private PostApplicationService postApplicationService;

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    @DisplayName("POST /posts/{postId}/comments: 댓글을 등록한다")
    void create() throws Exception {
        TestSecurityUtils.setMockUser(1L);
        given(commentApplicationService.createComment(any(CreateCommentCommand.class))).willReturn(1L);

        mockMvc.perform(post("/posts/1/comments")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", "댓글 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/1#comments"));

        verify(commentApplicationService).createComment(any(CreateCommentCommand.class));
    }

    @Test
    @DisplayName("POST /posts/{postId}/comments: 유효성 검증 실패 시 상세 페이지로 돌아간다")
    void create_validationError() throws Exception {
        TestSecurityUtils.setMockUser(1L);
        User author = User.create("user1", "encoded");
        Post post = Post.create("제목", "내용", author);
        given(postApplicationService.getPost(1L)).willReturn(post);
        given(commentApplicationService.getCommentsByPostId(1L)).willReturn(List.of());

        mockMvc.perform(post("/posts/1/comments")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"));
    }
}
