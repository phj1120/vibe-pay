package com.api.app.repository.member;

import com.api.app.entity.MemberBase;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface MemberBaseTrxMapper {

    /**
     * 회원번호 시퀀스 생성
     *
     * @return 생성된 회원번호
     */
    String generateMemberNo();

    /**
     * 회원 등록
     *
     * @param memberBase 회원 정보
     * @return 등록된 건수
     */
    int insertMemberBase(MemberBase memberBase);

    /**
     * 회원 수정
     *
     * @param memberBase 회원 정보
     * @return 수정된 건수
     */
    int updateMemberBase(MemberBase memberBase);

    /**
     * 회원 삭제
     *
     * @param memberNo 회원번호
     * @return 삭제된 건수
     */
    int deleteMemberBase(String memberNo);
}
