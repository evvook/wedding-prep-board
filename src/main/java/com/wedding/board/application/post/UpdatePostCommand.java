package com.wedding.board.application.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostCommand {

    private String title;
    private String content;
    private String location;
    private Integer mealPrice;
    private Integer guaranteeMin;
    private Integer rentalFee;
    private Integer etcFee;
}
