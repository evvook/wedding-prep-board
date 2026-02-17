package com.wedding.board.domain.post;

import com.wedding.board.domain.board.Board;
import com.wedding.board.domain.user.User;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_code")
    private Board board;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 예식장 전용 필드 (VENUE 보드에서 사용) */
    @Column(length = 200)
    private String location;

    private Integer mealPrice;

    private Integer guaranteeMin;

    private Integer rentalFee;

    private Integer etcFee;

    private Post(Board board, String title, String content, User author,
                 String location, Integer mealPrice, Integer guaranteeMin, Integer rentalFee, Integer etcFee) {
        this.board = board;
        this.title = title;
        this.content = content;
        this.author = author;
        this.location = location;
        this.mealPrice = mealPrice;
        this.guaranteeMin = guaranteeMin;
        this.rentalFee = rentalFee;
        this.etcFee = etcFee;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Post create(Board board, String title, String content, User author,
                             String location, Integer mealPrice, Integer guaranteeMin, Integer rentalFee, Integer etcFee) {
        return new Post(board, title, content, author, location, mealPrice, guaranteeMin, rentalFee, etcFee);
    }

    public void update(String title, String content, String location, Integer mealPrice,
                       Integer guaranteeMin, Integer rentalFee, Integer etcFee) {
        this.title = title;
        this.content = content;
        this.location = location;
        this.mealPrice = mealPrice;
        this.guaranteeMin = guaranteeMin;
        this.rentalFee = rentalFee;
        this.etcFee = etcFee;
        this.updatedAt = LocalDateTime.now();
    }

    /** board가 null인 기존 글 호환용 */
    public String getBoardCode() {
        return board != null ? board.getCode() : "GENERAL";
    }

    public boolean isVenue() {
        return "VENUE".equals(getBoardCode());
    }
}
