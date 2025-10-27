package com.api.app.repository.code;

import com.api.app.entity.CodeDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface CodeDetailMapper {

    /**
     * 공통코드상세 전체 조회
     *
     * @return 공통코드상세 목록
     */
    List<CodeDetail> selectAllCodeDetail();

    /**
     * 그룹코드로 조회
     *
     * @param groupCode 그룹코드
     * @return 공통코드상세 목록
     */
    List<CodeDetail> selectCodeDetailByGroupCode(String groupCode);

    /**
     * 복합키로 단건 조회
     *
     * @param groupCode 그룹코드
     * @param code      코드
     * @return 공통코드상세 정보
     */
    CodeDetail selectCodeDetailByPk(
            @Param("groupCode") String groupCode,
            @Param("code") String code
    );
}
