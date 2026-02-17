package com.wedding.board.web;

import com.wedding.board.application.comment.CommentApplicationService;
import com.wedding.board.application.post.CreatePostCommand;
import java.util.List;
import com.wedding.board.application.post.PostApplicationService;
import com.wedding.board.application.post.UpdatePostCommand;
import com.wedding.board.domain.comment.Comment;
import com.wedding.board.domain.post.Post;
import com.wedding.board.security.CustomUserDetails;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostApplicationService postApplicationService;
    private final CommentApplicationService commentApplicationService;

    @GetMapping
    public String list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        Page<Post> posts = postApplicationService.getPosts(pageable);
        model.addAttribute("posts", posts);
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @RequestParam(required = false) Long editComment,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        Post post = postApplicationService.getPost(id);
        List<Comment> comments = commentApplicationService.getCommentsByPostId(id);
        int commentCount = countCommentsRecursive(comments);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentCount", commentCount);
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("editCommentId", editComment);
        if (editComment != null) {
            Comment editTarget = commentApplicationService.getComment(editComment);
            model.addAttribute("editCommentForm", new CommentForm(editTarget.getContent(), null));
        } else {
            model.addAttribute("editCommentForm", new CommentForm());
        }
        if (userDetails != null) {
            model.addAttribute("currentUserId", userDetails.getId());
        }
        return "posts/detail";
    }

    private int countCommentsRecursive(java.util.List<Comment> comments) {
        int count = 0;
        for (Comment c : comments) {
            count += 1 + countCommentsRecursive(c.getReplies());
        }
        return count;
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("post", null);
        return "posts/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute PostForm postForm,
            BindingResult result,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        if (result.hasErrors()) {
            return "posts/form";
        }
        CreatePostCommand command = new CreatePostCommand(
                postForm.getTitle(),
                postForm.getContent(),
                userDetails.getId()
        );
        Long postId = postApplicationService.createPost(command);
        return "redirect:/posts/" + postId;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postApplicationService.getPost(id);
        model.addAttribute("post", post);
        model.addAttribute("postForm", new PostForm(post.getTitle(), post.getContent()));
        return "posts/form";
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody PostForm postForm) {
        if (postForm.getTitle() == null || postForm.getTitle().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "제목을 입력해주세요");
            return ResponseEntity.badRequest().body(err);
        }
        if (postForm.getContent() == null || postForm.getContent().isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "내용을 입력해주세요");
            return ResponseEntity.badRequest().body(err);
        }
        UpdatePostCommand command = new UpdatePostCommand(postForm.getTitle(), postForm.getContent());
        postApplicationService.updatePost(id, command);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("id", id);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        postApplicationService.deletePost(id);
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        return ResponseEntity.ok(body);
    }
}
