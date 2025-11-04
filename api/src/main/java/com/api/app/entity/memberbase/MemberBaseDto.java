package com.api.app.entity.memberbase;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberBaseDto {
    private String memberNo;
    private String memberName;
    private String phone;
    private String email;
    private String password;
    private String memberStatusCode;
    private String registId;
    private LocalDateTime registDateTime;
    private String modifyId;
    private LocalDateTime modifyDateTime;
}
