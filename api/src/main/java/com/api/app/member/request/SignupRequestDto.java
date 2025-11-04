package com.api.app.member.request;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String memberName;
    private String phone;
    private String email;
    private String password;
}
