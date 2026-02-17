package com.wedding.board.application.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostCommand {

    private String title;
    private String content;
    private Long authorId;
}
