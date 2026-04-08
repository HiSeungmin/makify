package com.xladmt.makify.common.config.security;

import com.xladmt.makify.common.jwt.JwtAuthenticationFilter;
import com.xladmt.makify.common.jwt.JwtLoginFilter;
import com.xladmt.makify.common.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final MemberDetailsService memberDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        JwtLoginFilter jwtLoginFilter = new JwtLoginFilter(authenticationManager, jwtUtil, redisTemplate);
        jwtLoginFilter.setFilterProcessesUrl("/auth/login");

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/auth/login", "/signup", "/css/**", "/js/**", "/images/**", "/videos/**").permitAll()
                        .requestMatchers("POST", "/signup").permitAll()
                        .requestMatchers("/auth/reissue").permitAll()
                        .requestMatchers("GET", "/feed").permitAll()
                        .requestMatchers("POST", "/api/feed/*/like").authenticated()
                        .requestMatchers("GET", "/api/feed/*/comments").permitAll()
                        .requestMatchers("POST", "/api/feed/*/comments").authenticated()
                        .requestMatchers("DELETE", "/api/feed/comments/*").authenticated()
                        .requestMatchers("GET", "/challenges").permitAll()
                        .requestMatchers("GET", "/api/challenges/search").permitAll()
                        .requestMatchers("GET", "/api/search/autocomplete").permitAll()
                        .requestMatchers("GET", "/challenges/new").authenticated()
                        .requestMatchers("GET", "/challenges/{id}").permitAll()
                        .requestMatchers("POST", "/challenges/new").authenticated()
                        .requestMatchers("GET", "/challenges/{id}/join").authenticated()
                        .requestMatchers("GET", "/challenges/{id}/verify").authenticated()
                        .requestMatchers("POST", "/challenges/{id}/verify").authenticated()
                        .requestMatchers("GET", "/challenges/{id}/history").authenticated()
                        .requestMatchers("GET", "/api/challenges/{id}/verify/validate").authenticated()
                        .requestMatchers("POST", "/api/challenges/{id}/verify-code").authenticated()
                        .requestMatchers("GET", "/mypage").authenticated()
                        .requestMatchers("/notifications").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterAt(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();
                            // API 요청은 401 JSON 응답, 페이지 요청은 로그인으로 리다이렉트
                            if (requestURI.startsWith("/api/")) {
                                response.setStatus(401);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                                response.getWriter().write("{\"message\":\"로그인이 필요합니다.\"}");
                            } else {
                                response.sendRedirect("/login?redirectURL=" + requestURI);
                            }
                        })
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/videos/**")
                .requestMatchers("/favicon.ico")
                .requestMatchers("/webjars/**")
                .requestMatchers("/static/**");
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, memberDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
