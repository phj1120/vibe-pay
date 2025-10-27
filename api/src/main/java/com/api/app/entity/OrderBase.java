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
@Alias("OrderBase")
@Getter
@Setter
public class OrderBase implements Serializable {
    private static final long serialVersionUID = 2345678901234567890L;

    @Schema(description = "주문번호")
    private String orderNo;

    @Schema(description = "회원번호")
    private String memberNo;
}
