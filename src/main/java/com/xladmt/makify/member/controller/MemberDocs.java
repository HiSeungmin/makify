package com.xladmt.makify.member.controller;

import com.xladmt.makify.member.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
@Tag(name = "Login Page API", description = "APIs Related to Login Page")

public interface MemberDocs {
    @Operation(
            summary = "회원 로그인",
            description = "회원 로그인 처리 및 JWT 토큰 발급. 발급된 Access Token과 Refresh Token은 쿠키로 반환됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "로그인 실패")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원 로그인 요청 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
    )
    @PostMapping("/login")
    ResponseEntity<String> login(@RequestBody LoginRequest request);

}
