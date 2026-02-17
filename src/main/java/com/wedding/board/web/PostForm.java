package com.wedding.board.web;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostForm {

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;
}
