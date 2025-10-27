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
@Alias("CodeDetail")
@Getter
@Setter
public class CodeDetail implements Serializable {
    private static final long serialVersionUID = 8901234567890123456L;

    @Schema(description = "그룹코드")
    private String groupCode;

    @Schema(description = "코드")
    private String code;

    @Schema(description = "코드명")
    private String codeName;

    @Schema(description = "참조값1")
    private String referenceValue1;

    @Schema(description = "참조값2")
    private String referenceValue2;

    @Schema(description = "정렬순서")
    private Long displaySequence;
}
