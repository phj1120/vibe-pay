package com.vibe.pay.domain.member.repository;

import com.vibe.pay.domain.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {
    Long getNextMemberSequence();
    void insert(Member member);
    Optional<Member> findByMemberId(Long memberId);
    List<Member> findAll();
    void update(Member member);
    void delete(Long memberId);
}
