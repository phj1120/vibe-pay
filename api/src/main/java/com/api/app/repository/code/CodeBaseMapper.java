package com.api.app.repository.code;

import com.api.app.entity.CodeBase;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface CodeBaseMapper {

    /**
     * 공통코드 전체 조회
     *
     * @return 공통코드 목록
     */
    List<CodeBase> selectAllCodeBase();

    /**
     * 그룹코드로 조회
     *
     * @param groupCode 그룹코드
     * @return 공통코드 정보
     */
    CodeBase selectCodeBaseByGroupCode(String groupCode);
}
