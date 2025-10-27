package com.api.app.repository.code;

import com.api.app.entity.CodeBase;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface CodeBaseTrxMapper {

    /**
     * 공통코드 등록
     *
     * @param codeBase 공통코드 정보
     * @return 등록된 건수
     */
    int insertCodeBase(CodeBase codeBase);

    /**
     * 공통코드 수정
     *
     * @param codeBase 공통코드 정보
     * @return 수정된 건수
     */
    int updateCodeBase(CodeBase codeBase);

    /**
     * 공통코드 삭제
     *
     * @param groupCode 그룹코드
     * @return 삭제된 건수
     */
    int deleteCodeBase(String groupCode);
}
