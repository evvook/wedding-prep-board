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

    private String boardCode;

    @NotBlank(message = "제목을 입력해주세요")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    /** 예식장(VENUE) 전용 필드 */
    private String location;
    private Integer mealPrice;
    private Integer guaranteeMin;
    private Integer rentalFee;
    private Integer etcFee;

    public PostForm(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
