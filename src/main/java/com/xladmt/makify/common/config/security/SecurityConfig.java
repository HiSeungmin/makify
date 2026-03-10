package com.xladmt.makify.common.config.security;

import com.xladmt.makify.common.jwt.JwtAuthenticationFilter;
import com.xladmt.makify.common.jwt.JwtLoginFilter;
import com.xladmt.makify.common.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
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
                        .requestMatchers("GET", "/challenges").permitAll()       // 챌린지 목록
                        .requestMatchers("GET", "/api/challenges/search").permitAll()   // 챌린지 검색 API
                        .requestMatchers("GET", "/api/search/autocomplete").permitAll() // 자동완성 API
                        .requestMatchers("GET", "/challenges/new").authenticated()      // 챌린지 생성 폼 (반드시 {id} 패턴보다 위에 위치해야 함)
                        .requestMatchers("GET", "/challenges/{id}").permitAll()         // 챌린지 상세 (비로그인 허용)
                        .requestMatchers("POST", "/challenges/new").authenticated()     // 챌린지 생성
                        .requestMatchers("GET", "/challenges/{id}/join").authenticated() // 챌린지 참여
                        .requestMatchers("GET", "/challenges/{id}/verify").authenticated() // 챌린지 인증 페이지
                        .requestMatchers("GET", "/api/challenges/{id}/verify/validate").authenticated() // 인증 시간 검증
                        .requestMatchers("GET", "/mypage").authenticated()              // 마이페이지
                        .anyRequest().authenticated()
                )
                .addFilterAt(jwtLoginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 미인증 접근 시 로그인 페이지로 리다이렉트
                            response.sendRedirect("/login?redirectURL=" + request.getRequestURI());
                        })
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 정적 리소스는 아예 Security 필터를 거치지 않음
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
        configuration.addAllowedOriginPattern("*"); // 모든 도메인 허용 (개발용)
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 쿠키/인증 정보 포함 허용
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}