package com.xladmt.makify.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String loginId;
    private String password;
}
