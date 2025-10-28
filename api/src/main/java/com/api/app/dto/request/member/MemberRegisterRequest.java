package com.api.app.dto.request.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Alias("MemberRegisterRequest")
@Getter
@Setter
public class MemberRegisterRequest implements Serializable {
    private static final long serialVersionUID = 1234567890123456789L;

    @Schema(description = "회원명", example = "홍길동")
    @NotBlank(message = "회원명은 필수입니다")
    private String memberName;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @Schema(description = "이메일", example = "hong@example.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;
}
