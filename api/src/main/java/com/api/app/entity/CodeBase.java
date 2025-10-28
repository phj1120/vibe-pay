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
@Alias("CodeBase")
@Getter
@Setter
public class CodeBase extends SystemEntity {
    private static final long serialVersionUID = 7890123456789012345L;

    @Schema(description = "그룹코드")
    private String groupCode;

    @Schema(description = "그룹코드명")
    private String groupCodeName;
}
