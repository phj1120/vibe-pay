package com.vibe.pay.backend.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@RestController
@RequestMapping("/api/members")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemberController {
    private final MemberService memberService;

    /**
     * GET /api/members
     * Get all members
     */
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        log.debug("GET /api/members - Get all members");
        List<Member> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    /**
     * GET /api/members/{id}
     * Get member by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        log.debug("GET /api/members/{} - Get member by ID", id);
        return memberService.getMemberById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/members
     * Create new member
     */
    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody MemberRequest request) {
        log.debug("POST /api/members - Create new member: {}", request.getName());
        try {
            Member createdMember = memberService.createMember(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
        } catch (IllegalArgumentException e) {
            log.error("Invalid member data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/members/{id}
     * Update member information
     */
    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(
            @PathVariable Long id,
            @RequestBody MemberRequest request) {
        log.debug("PUT /api/members/{} - Update member", id);
        try {
            Member updatedMember = memberService.updateMember(id, request);
            return ResponseEntity.ok(updatedMember);
        } catch (MemberService.MemberNotFoundException e) {
            log.error("Member not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/members/{id}
     * Delete member
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.debug("DELETE /api/members/{} - Delete member", id);
        try {
            memberService.deleteMember(id);
            return ResponseEntity.noContent().build();
        } catch (MemberService.MemberNotFoundException e) {
            log.error("Member not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (MemberService.MemberHasOrdersException e) {
            log.error("Cannot delete member with orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
