package com.wedding.board.config;

import com.wedding.board.domain.user.User;
import com.wedding.board.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User user1 = User.create("user1", passwordEncoder.encode("1234"));
        User user2 = User.create("user2", passwordEncoder.encode("1234"));
        userRepository.save(user1);
        userRepository.save(user2);
    }
}
