package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
@Alias("PayBase")
@Getter
@Setter
public class PayBase implements Serializable {
    private static final long serialVersionUID = 5678901234567890123L;

    @Schema(description = "결제번호")
    private String payNo;

    @Schema(description = "결제유형코드")
    private String payTypeCode;

    @Schema(description = "결제방식코드")
    private String payWayCode;

    @Schema(description = "결제상태코드")
    private String payStatusCode;

    @Schema(description = "승인번호")
    private String approveNo;

    @Schema(description = "주문번호")
    private String orderNo;

    @Schema(description = "클레임번호")
    private String claimNo;

    @Schema(description = "상위결제번호")
    private String upperPayNo;

    @Schema(description = "거래번호")
    private String trdNo;

    @Schema(description = "결제완료일시")
    private LocalDateTime payFinishDateTime;

    @Schema(description = "회원번호")
    private String memberNo;

    @Schema(description = "결제금액")
    private Long amount;

    @Schema(description = "취소가능금액")
    private Long cancelableAmount;
}
