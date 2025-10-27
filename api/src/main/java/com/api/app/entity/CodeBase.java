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
@Alias("CodeBase")
@Getter
@Setter
public class CodeBase implements Serializable {
    private static final long serialVersionUID = 7890123456789012345L;

    @Schema(description = "그룹코드")
    private String groupCode;

    @Schema(description = "그룹코드명")
    private String groupCodeName;
}
