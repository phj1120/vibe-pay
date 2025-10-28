package com.api.app.dto.response.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Alias("MemberLoginResponse")
@Getter
@Setter
@AllArgsConstructor
public class MemberLoginResponse implements Serializable {
    private static final long serialVersionUID = 3456789012345678901L;

    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "리프레시 토큰")
    private String refreshToken;
}
