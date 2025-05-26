package com.xladmt.makify.common.config;

// Swagger UI 주소입니다.
// http://localhost:8080/swagger-ui/index.html#/

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Makify API").version("1.0.0")
                        .description("Makify API 문서 백엔드 개발용"));
    }

    @Bean
    public GroupedOpenApi ttsApi() {
        return GroupedOpenApi.builder()
                .group("Member API")
                .pathsToMatch("/member/**") // '/member'로 시작하는 모든 엔드포인트를 포함
                .build();
    }

    @Bean
    public GroupedOpenApi vcApi() {
        return GroupedOpenApi.builder()
                .group("VC API")
                .pathsToMatch("/vc/**") // '/vc'로 시작하는 모든 엔드포인트를 포함
                .build();
    }

    @Bean
    public GroupedOpenApi concatApi() {
        return GroupedOpenApi.builder()
                .group("Concat API")
                .pathsToMatch("/concat/**") // '/concat'로 시작하는 모든 엔드포인트를 포함
                .build();
    }

    @Bean
    public GroupedOpenApi s3Api() {
        return GroupedOpenApi.builder()
                .group("S3 Test")
                .pathsToMatch("/s3_test/**") // '/s3_test'로 시작하는 모든 엔드포인트를 포함
                .build();
    }

    @Bean
    public GroupedOpenApi workspaceApi() {
        return GroupedOpenApi.builder()
                .group("Workspace API")
                .pathsToMatch("/workspace/**") // '/workspace'로 시작하는 모든 엔드포인트를 포함
                .build();
    }

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
                .group("Member API")
                .pathsToMatch("/member/**") // '/member'로 시작하는 모든 엔드포인트를 포함
                .build();
    }

    @Bean
    public GroupedOpenApi taskApi() {
        return GroupedOpenApi.builder()
                .group("Task API")
                .pathsToMatch("/task/**") // '/task'로 시작하는 모든 엔드포인트를 포함
                .build();
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("Default API") // 특정 그룹에 포함되지 않은 엔드포인트
                .pathsToExclude("/tts/**", "/vc/**", "/concat/**", "/s3_test/**", "/workspace/**", "/member/**",
                        "/task/**") // 다른 그룹들에서 제외된 엔드포인트만 포함
                .build();
    }
}
