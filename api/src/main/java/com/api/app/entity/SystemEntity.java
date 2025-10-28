package com.api.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 시스템 공통 컬럼을 관리하는 기본 엔티티
 * 모든 엔티티는 이 클래스를 상속받아 공통 컬럼(등록자, 등록일시, 수정자, 수정일시)을 관리합니다.
 *
 * @author system
 * @version 1.0
 * @since 2025-10-28
 */
@Getter
@Setter
public abstract class SystemEntity implements Serializable {
    private static final long serialVersionUID = 1000000000000000001L;

    @Schema(description = "등록자")
    private String registId;

    @Schema(description = "등록일시")
    private LocalDateTime registDateTime;

    @Schema(description = "수정자")
    private String modifyId;

    @Schema(description = "수정일시")
    private LocalDateTime modifyDateTime;
}
