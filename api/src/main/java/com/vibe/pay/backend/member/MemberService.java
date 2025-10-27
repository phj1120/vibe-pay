package com.vibe.pay.backend.member;

import com.vibe.pay.backend.rewardpoints.RewardPoints;
import com.vibe.pay.backend.rewardpoints.RewardPointsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;
    private final RewardPointsMapper rewardPointsMapper;

    /**
     * 회원 생성
     * - 회원 생성 시 자동으로 RewardPoints 엔티티 생성 (초기 포인트 0)
     */
    @Transactional
    public Member createMember(MemberRequest request) {
        log.debug("Creating member: {}", request.getName());

        // Validate name
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Member name is required");
        }

        // Check if email already exists
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // TODO: Implement email uniqueness check when we have a selectByEmail method
        }

        // Create Member entity
        Member member = Member.builder()
                .name(request.getName())
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .build();

        memberMapper.insert(member);
        log.info("Member created with ID: {}", member.getMemberId());

        // Automatically create RewardPoints entity with 0 initial points
        RewardPoints rewardPoints = RewardPoints.builder()
                .memberId(member.getMemberId())
                .points(0.0)
                .lastUpdated(LocalDateTime.now())
                .build();

        rewardPointsMapper.insert(rewardPoints);
        log.debug("RewardPoints created for member ID: {}", member.getMemberId());

        return memberMapper.selectById(member.getMemberId());
    }

    /**
     * 회원 ID로 조회
     */
    public Optional<Member> getMemberById(Long memberId) {
        log.debug("Getting member by ID: {}", memberId);
        Member member = memberMapper.selectById(memberId);
        return Optional.ofNullable(member);
    }

    /**
     * 모든 회원 조회
     */
    public List<Member> getAllMembers() {
        log.debug("Getting all members");
        return memberMapper.selectAll();
    }

    /**
     * 회원 정보 수정
     */
    @Transactional
    public Member updateMember(Long memberId, MemberRequest request) {
        log.debug("Updating member ID: {}", memberId);

        // Verify member exists
        Member existingMember = memberMapper.selectById(memberId);
        if (existingMember == null) {
            throw new MemberNotFoundException("Member not found with id: " + memberId);
        }

        // Update member fields
        Member updatedMember = Member.builder()
                .memberId(memberId)
                .name(request.getName())
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .createdAt(existingMember.getCreatedAt())
                .build();

        memberMapper.update(updatedMember);
        log.info("Member updated with ID: {}", memberId);

        return memberMapper.selectById(memberId);
    }

    /**
     * 회원 삭제
     * - 주문 내역이 있는 회원은 삭제 불가
     */
    @Transactional
    public void deleteMember(Long memberId) {
        log.debug("Deleting member ID: {}", memberId);

        // Verify member exists
        Member existingMember = memberMapper.selectById(memberId);
        if (existingMember == null) {
            throw new MemberNotFoundException("Member not found with id: " + memberId);
        }

        // Check if member has active orders
        // TODO: Implement order check when OrderMapper is available
        // List<Order> orders = orderMapper.selectByMemberId(memberId);
        // if (!orders.isEmpty()) {
        //     throw new MemberHasOrdersException("Cannot delete member with existing orders");
        // }

        memberMapper.delete(memberId);
        log.info("Member deleted with ID: {}", memberId);
    }

    /**
     * Custom exception for member not found
     */
    public static class MemberNotFoundException extends RuntimeException {
        public MemberNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Custom exception for member with orders
     */
    public static class MemberHasOrdersException extends RuntimeException {
        public MemberHasOrdersException(String message) {
            super(message);
        }
    }
}
