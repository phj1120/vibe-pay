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
@Alias("GoodsBase")
@Getter
@Setter
public class GoodsBase implements Serializable {
    private static final long serialVersionUID = 2234567890123456789L;

    @Schema(description = "상품번호")
    private String goodsNo;

    @Schema(description = "상품명")
    private String goodsName;

    @Schema(description = "상품상태코드")
    private String goodsStatusCode;
}
