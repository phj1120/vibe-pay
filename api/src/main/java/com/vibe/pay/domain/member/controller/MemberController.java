package com.vibe.pay.domain.member.controller;

import com.vibe.pay.domain.member.dto.MemberRequest;
import com.vibe.pay.domain.member.dto.MemberResponse;
import com.vibe.pay.domain.member.entity.Member;
import com.vibe.pay.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원 관리 컨트롤러
 * 회원 CRUD 및 회원별 포인트/주문 내역 조회 API를 제공합니다.
 *
 * @author Vibe Pay Team
 * @version 1.0
 * @since 2025-10-16
 */
@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 생성
     *
     * @param request 회원 정보
     * @return 생성된 회원 정보
     */
    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@RequestBody MemberRequest request) {
        log.info("Creating member: {}", request.getName());
        Member member = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(member));
    }

    /**
     * 모든 회원 조회
     *
     * @return 회원 목록
     */
    @GetMapping
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        log.info("Getting all members");
        List<Member> members = memberService.getAllMembers();
        List<MemberResponse> responses = members.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 회원 조회
     *
     * @param memberId 회원 ID
     * @return 회원 정보
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long memberId) {
        log.info("Getting member by ID: {}", memberId);
        return memberService.getMemberById(memberId)
                .map(member -> ResponseEntity.ok(toResponse(member)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 회원 정보 수정
     *
     * @param memberId 회원 ID
     * @param request  수정할 회원 정보
     * @return 수정된 회원 정보
     */
    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable Long memberId,
            @RequestBody MemberRequest request) {
        log.info("Updating member ID: {}", memberId);
        try {
            Member member = memberService.updateMember(memberId, request);
            return ResponseEntity.ok(toResponse(member));
        } catch (IllegalArgumentException e) {
            log.warn("Member not found: {}", memberId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 회원 삭제
     *
     * @param memberId 회원 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
        log.info("Deleting member ID: {}", memberId);
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Member 엔티티를 MemberResponse DTO로 변환
     *
     * @param member Member 엔티티
     * @return MemberResponse DTO
     */
    private MemberResponse toResponse(Member member) {
        MemberResponse response = new MemberResponse();
        response.setMemberId(member.getMemberId());
        response.setName(member.getName());
        response.setShippingAddress(member.getShippingAddress());
        response.setPhoneNumber(member.getPhoneNumber());
        response.setEmail(member.getEmail());
        response.setCreatedAt(member.getCreatedAt());
        return response;
    }
}
