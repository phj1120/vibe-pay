package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
@Alias("PayInterfaceLog")
@Getter
@Setter
public class PayInterfaceLog implements Serializable {
    private static final long serialVersionUID = 6789012345678901234L;

    @Schema(description = "결제인터페이스번호")
    private String payInterfaceNo;

    @Schema(description = "회원번호")
    private String memberNo;

    @Schema(description = "결제번호")
    private String payNo;

    @Schema(description = "로그유형코드")
    private String payLogCode;

    @Schema(description = "요청JSON")
    private String requestJson;

    @Schema(description = "응답JSON")
    private String responseJson;
}
