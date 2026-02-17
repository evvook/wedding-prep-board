package com.wedding.board.web;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentForm {

    @NotBlank(message = "댓글 내용을 입력해주세요")
    private String content;

    private Long parentId;  // null이면 최상위, 있으면 대댓글
}
