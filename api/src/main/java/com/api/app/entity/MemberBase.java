package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
@Alias("MemberBase")
@Getter
@Setter
public class MemberBase extends SystemEntity {
    private static final long serialVersionUID = 9012345678901234567L;

    @Schema(description = "회원번호")
    private String memberNo;

    @Schema(description = "회원명")
    private String memberName;

    @Schema(description = "전화번호")
    private String phone;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "비밀번호")
    private String password;

    @Schema(description = "회원상태코드")
    private String memberStatusCode;
}
