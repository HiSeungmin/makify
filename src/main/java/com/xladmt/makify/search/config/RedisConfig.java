package com.xladmt.makify.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Trie 데이터베이스를 위한 Redis 설정
 */
@Configuration
public class RedisConfig {

    /**
     * Redis 전용 ObjectMapper (타입 정보 포함)
     * Trie 직렬화/역직렬화에만 사용
     */
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 자동 타입 감지 활성화 (다형성 처리)
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        return objectMapper;
    }

    /**
     * 기본 ObjectMapper (타입 정보 없음)
     * API 응답 등 일반 용도
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * RedisTemplate<String, Object> 빈 등록
     * 키: String 직렬화
     * 값: String 직렬화 (수동으로 JSON 변환 처리)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 문자열 직렬화만 사용 (타입 정보 제외)
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 키 직렬화 설정
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 값도 문자열로 (수동으로 JSON 변환)
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
