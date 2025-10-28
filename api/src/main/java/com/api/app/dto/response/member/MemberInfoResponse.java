package com.api.app.dto.response.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Alias("MemberInfoResponse")
@Getter
@Setter
public class MemberInfoResponse implements Serializable {
    private static final long serialVersionUID = 4567890123456789012L;

    @Schema(description = "회원명")
    private String memberName;

    @Schema(description = "전화번호")
    private String phone;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "회원상태코드")
    private String memberStatusCode;
}
