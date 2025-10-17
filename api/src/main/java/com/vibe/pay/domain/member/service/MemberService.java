package com.vibe.pay.domain.member.service;

import com.vibe.pay.domain.member.dto.MemberRequest;
import com.vibe.pay.domain.member.entity.Member;
import com.vibe.pay.domain.member.repository.MemberMapper;
import com.vibe.pay.domain.rewardpoints.service.RewardPointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 회원 서비스
 * 회원 관리 및 리워드 포인트 초기화를 처리하는 비즈니스 로직 계층
 *
 * @see Member
 * @see MemberMapper
 * @see RewardPointsService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final RewardPointsService rewardPointsService;

    /**
     * 회원 생성
     * 새로운 회원을 등록하고 초기 리워드 포인트를 생성합니다.
     *
     * @param request 회원 생성 요청 DTO
     * @return 생성된 회원 엔티티
     * @throws RuntimeException 회원 생성 실패 시
     */
    @Transactional
    public Member createMember(MemberRequest request) {
        log.info("Creating new member: name={}", request.getName());

        // 시퀀스에서 ID 생성
        Long memberId = memberMapper.getNextMemberSequence();

        Member member = new Member();
        member.setMemberId(memberId);
        member.setName(request.getName());
        member.setShippingAddress(request.getShippingAddress());
        member.setPhoneNumber(request.getPhoneNumber());
        member.setEmail(request.getEmail());
        member.setCreatedAt(LocalDateTime.now());

        memberMapper.insert(member);
        log.info("Member created successfully: memberId={}", member.getMemberId());

        // 초기 리워드 포인트 생성 (0 포인트)
        rewardPointsService.addPoints(member.getMemberId(), 0L);
        log.info("Initial reward points created for member: memberId={}", member.getMemberId());

        return member;
    }

    /**
     * 회원 ID로 회원 조회
     *
     * @param memberId 회원 ID
     * @return 조회된 회원 엔티티 (Optional)
     */
    public Optional<Member> getMemberById(Long memberId) {
        log.debug("Fetching member by ID: memberId={}", memberId);
        return memberMapper.findByMemberId(memberId);
    }

    /**
     * 전체 회원 목록 조회
     *
     * @return 회원 엔티티 목록
     */
    public List<Member> getAllMembers() {
        log.debug("Fetching all members");
        return memberMapper.findAll();
    }

    /**
     * 회원 정보 수정
     *
     * @param memberId 수정할 회원 ID
     * @param request 회원 수정 요청 DTO
     * @return 수정된 회원 엔티티
     * @throws RuntimeException 회원을 찾을 수 없거나 수정 실패 시
     */
    @Transactional
    public Member updateMember(Long memberId, MemberRequest request) {
        log.info("Updating member: memberId={}", memberId);

        Member member = memberMapper.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));

        member.setName(request.getName());
        member.setShippingAddress(request.getShippingAddress());
        member.setPhoneNumber(request.getPhoneNumber());
        member.setEmail(request.getEmail());

        memberMapper.update(member);
        log.info("Member updated successfully: memberId={}", memberId);

        return member;
    }

    /**
     * 회원 삭제
     *
     * @param memberId 삭제할 회원 ID
     * @throws RuntimeException 회원을 찾을 수 없거나 삭제 실패 시
     */
    @Transactional
    public void deleteMember(Long memberId) {
        log.info("Deleting member: memberId={}", memberId);

        Member member = memberMapper.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found: " + memberId));

        memberMapper.delete(memberId);
        log.info("Member deleted successfully: memberId={}", memberId);
    }
}
