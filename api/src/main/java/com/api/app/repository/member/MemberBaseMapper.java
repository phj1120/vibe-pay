package com.api.app.repository.member;

import com.api.app.entity.MemberBase;

import java.util.List;

/**
 * @author system
 * @version 1.0
 * @since 2025-10-27
 */
public interface MemberBaseMapper {

    /**
     * 회원 전체 조회
     *
     * @return 회원 목록
     */
    List<MemberBase> selectAllMemberBase();

    /**
     * 회원번호로 조회
     *
     * @param memberNo 회원번호
     * @return 회원 정보
     */
    MemberBase selectMemberBaseByMemberNo(String memberNo);

    /**
     * 이메일로 조회
     *
     * @param email 이메일
     * @return 회원 정보
     */
    MemberBase selectMemberBaseByEmail(String email);
}
