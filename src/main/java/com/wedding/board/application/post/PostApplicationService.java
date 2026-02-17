package com.wedding.board.application.post;

import com.wedding.board.domain.comment.CommentRepository;
import com.wedding.board.domain.post.Post;
import com.wedding.board.domain.post.PostRepository;
import com.wedding.board.domain.user.User;
import com.wedding.board.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostApplicationService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public Long createPost(CreatePostCommand command) {
        User author = userRepository.findById(command.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        Post post = Post.create(command.getTitle(), command.getContent(), author);
        return postRepository.save(post).getId();
    }

    @Transactional
    public void updatePost(Long id, UpdatePostCommand command) {
        Post post = getPost(id);
        post.update(command.getTitle(), command.getContent());
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        commentRepository.deleteByPostIdAndParentIsNotNull(id);
        commentRepository.deleteByPostIdAndParentIsNull(id);
        postRepository.deleteById(id);
    }
}
