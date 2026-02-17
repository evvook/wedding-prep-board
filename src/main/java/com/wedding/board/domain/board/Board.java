package com.wedding.board.domain.board;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

    @Id
    @Column(length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private Board(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Board of(String code, String name) {
        return new Board(code, name);
    }
}
