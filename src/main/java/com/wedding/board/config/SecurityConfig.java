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
                .antMatchers(HttpMethod.GET, "/posts", "/posts/*").permitAll()
                .antMatchers("/posts/new", "/posts/*/edit").authenticated()
                .antMatchers(HttpMethod.POST, "/posts", "/posts/*").authenticated()
                .antMatchers(HttpMethod.PUT, "/posts/*", "/posts/*/comments/*").authenticated()
                .antMatchers(HttpMethod.DELETE, "/posts/*", "/posts/*/comments/*").authenticated()
                .antMatchers(HttpMethod.POST, "/posts/*/comments").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/posts", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/posts")
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
