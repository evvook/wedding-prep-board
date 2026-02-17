package com.wedding.board.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User 도메인")
class UserTest {

    @Test
    @DisplayName("create: 아이디와 암호화된 비밀번호로 사용자를 생성한다")
    void create() {
        User user = User.create("user1", "encodedPassword123");

        assertThat(user.getUsername()).isEqualTo("user1");
        assertThat(user.getPassword()).isEqualTo("encodedPassword123");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }
}
