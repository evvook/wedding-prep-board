package com.wedding.board.web;

import com.wedding.board.application.comment.CommentApplicationService;
import com.wedding.board.application.comment.CreateCommentCommand;
import com.wedding.board.application.comment.UpdateCommentCommand;
import com.wedding.board.application.post.PostApplicationService;
import com.wedding.board.domain.comment.Comment;
import com.wedding.board.domain.post.Post;
import com.wedding.board.security.CustomUserDetails;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/boards/{boardCode}/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentApplicationService commentApplicationService;
    private final PostApplicationService postApplicationService;

    @PostMapping
    public String create(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @Valid @ModelAttribute CommentForm commentForm,
            BindingResult result,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        if (result.hasErrors()) {
            Post post = postApplicationService.getPost(postId);
            List<Comment> comments = commentApplicationService.getCommentsByPostId(postId);
            model.addAttribute("boardCode", boardCode);
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            model.addAttribute("commentForm", commentForm);
            int commentCount = countCommentsRecursive(comments);
            model.addAttribute("commentCount", commentCount);
            model.addAttribute("currentUserId", userDetails.getId());
            if (commentForm.getParentId() != null) {
                model.addAttribute("expandReplyFormId", commentForm.getParentId());
            }
            model.addAttribute("editCommentId", (Long) null);
            model.addAttribute("editCommentForm", new CommentForm());
            return "posts/detail";
        }
        CreateCommentCommand command = new CreateCommentCommand(
                commentForm.getContent(),
                postId,
                userDetails.getId(),
                commentForm.getParentId()
        );
        commentApplicationService.createComment(command);
        return "redirect:/boards/" + boardCode + "/posts/" + postId + "#comments";
    }

    private int countCommentsRecursive(java.util.List<Comment> comments) {
        int count = 0;
        for (Comment c : comments) {
            count += 1 + countCommentsRecursive(c.getReplies());
        }
        return count;
    }

    @PutMapping(value = "/{commentId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentForm commentForm,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (commentForm.getContent() == null || commentForm.getContent().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "댓글 내용을 입력해주세요");
            return ResponseEntity.badRequest().body(err);
        }
        commentApplicationService.updateComment(
                commentId,
                new UpdateCommentCommand(commentForm.getContent()),
                userDetails.getId()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("content", commentForm.getContent());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping(value = "/{commentId}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable String boardCode,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentApplicationService.deleteComment(commentId, userDetails.getId());
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        return ResponseEntity.ok(body);
    }
}
