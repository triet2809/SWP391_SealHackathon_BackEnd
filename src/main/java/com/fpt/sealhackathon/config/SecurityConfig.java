package com.fpt.sealhackathon.config;

import com.fpt.sealhackathon.security.CustomUserDetailsService;
import com.fpt.sealhackathon.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
// Cau hinh Spring Security cho cac endpoint cong khai, endpoint can JWT va cac bean auth lien quan.
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    // Cau hinh security o muc tong the: endpoint nao duoc mo va endpoint nao bat buoc phai co JWT.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/register/fpt",
                                "/auth/register/external",
                                "/auth/login",
                                "/auth/refresh-token",
                                "/api/events/**",
                                "/api/rounds/**",
                                "/api/round-tracks/**",
                                "/api/criteria-templates/**",
                                "/api/round-criteria/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/guest-judges")
                        .hasAnyAuthority("coordinator", "COORDINATOR", "ROLE_COORDINATOR")
                        .requestMatchers("/api/users/**", "/api/roles/**").hasAuthority("coordinator")
                        .requestMatchers("/auth/me", "/auth/logout").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // PasswordEncoder dung BCrypt de password luu trong DB luon la hash thay vi plain text.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Noi Spring Security voi UserDetailsService tu viet de framework biet cach tai user tu DB.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    // Tra 401 ro rang khi client goi endpoint protected ma khong co JWT hop le.
    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Unauthorized\"}");
        };
    }
}
