package com.wedding.board.application.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentCommand {

    private String content;
    private Long postId;
    private Long authorId;
    private Long parentId;  // null이면 최상위 댓글, 있으면 대댓글
}
