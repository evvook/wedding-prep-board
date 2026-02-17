package com.wedding.board.web;

import com.wedding.board.domain.user.User;
import com.wedding.board.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestSecurityUtils {

    private TestSecurityUtils() {
    }

    public static void setMockUser(Long userId) {
        User user = User.create("user1", "encoded");
        ReflectionTestUtils.setField(user, "id", userId);
        CustomUserDetails details = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    public static void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
