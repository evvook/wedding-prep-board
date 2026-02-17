package com.wedding.board.config;

import com.wedding.board.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(auth -> auth
                .antMatchers("/", "/css/**", "/js/**", "/h2-console/**", "/login").permitAll()
                .antMatchers(HttpMethod.GET, "/boards/*/posts", "/boards/*/posts/*").permitAll()
                .antMatchers("/boards/*/posts/new", "/boards/*/posts/*/edit").authenticated()
                .antMatchers(HttpMethod.POST, "/boards/*/posts", "/boards/*/posts/*").authenticated()
                .antMatchers(HttpMethod.PUT, "/boards/*/posts/*", "/boards/*/posts/*/comments/*").authenticated()
                .antMatchers(HttpMethod.DELETE, "/boards/*/posts/*", "/boards/*/posts/*/comments/*").authenticated()
                .antMatchers(HttpMethod.POST, "/boards/*/posts/*/comments").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/boards/GENERAL/posts", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/boards/GENERAL/posts")
                .permitAll()
            )
            .userDetailsService(userDetailsService);

        http.csrf(csrf -> csrf.ignoringAntMatchers("/h2-console/**"));
        http.headers().frameOptions().sameOrigin();

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
