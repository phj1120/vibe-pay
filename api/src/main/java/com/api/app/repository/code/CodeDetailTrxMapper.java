package com.api.app.repository.code;

import com.api.app.entity.CodeDetail;
import org.apache.ibatis.annotations.Param;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface CodeDetailTrxMapper {

    /**
     * 공통코드상세 등록
     *
     * @param codeDetail 공통코드상세 정보
     * @return 등록된 건수
     */
    int insertCodeDetail(CodeDetail codeDetail);

    /**
     * 공통코드상세 수정
     *
     * @param codeDetail 공통코드상세 정보
     * @return 수정된 건수
     */
    int updateCodeDetail(CodeDetail codeDetail);

    /**
     * 공통코드상세 삭제 (복합키)
     *
     * @param groupCode 그룹코드
     * @param code      코드
     * @return 삭제된 건수
     */
    int deleteCodeDetail(
            @Param("groupCode") String groupCode,
            @Param("code") String code
    );
}
