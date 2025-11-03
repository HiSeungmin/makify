package com.xladmt.makify.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String name;
    private String nickname;
    private String email;
    private String loginId;
    private String password;
    private String phone;
}
