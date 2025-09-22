package com.vibe.pay.backend.member;

import com.vibe.pay.backend.pointhistory.PointHistory;
import com.vibe.pay.backend.pointhistory.PointHistoryService;
import com.vibe.pay.backend.order.OrderDetailDto;
import com.vibe.pay.backend.order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointHistoryService pointHistoryService;

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Member createMember(@RequestBody Member member) {
        return memberService.createMember(member);
    }

    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long memberId) {
        return memberService.getMemberById(memberId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<Member> updateMember(@PathVariable Long memberId, @RequestBody Member memberDetails) {
        try {
            Member updatedMember = memberService.updateMember(memberId, memberDetails);
            return ResponseEntity.ok(updatedMember);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{memberId}/point-history")
    public ResponseEntity<List<PointHistory>> getPointHistory(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<PointHistory> pointHistory = pointHistoryService.getPointHistoryByMemberWithPaging(memberId, page, size);
            return ResponseEntity.ok(pointHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{memberId}/order-history")
    public ResponseEntity<List<OrderDetailDto>> getOrderHistory(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<OrderDetailDto> orderHistory = orderService.getOrderDetailsWithPaymentsByMemberIdWithPaging(memberId, page, size);
            return ResponseEntity.ok(orderHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
