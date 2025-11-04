package com.api.app.member.request;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
