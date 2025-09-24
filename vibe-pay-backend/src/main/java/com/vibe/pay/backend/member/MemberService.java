package com.vibe.pay.backend.member;

import com.vibe.pay.backend.rewardpoints.RewardPoints;
import com.vibe.pay.backend.rewardpoints.RewardPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final RewardPointsService rewardPointsService;

    @Transactional // Ensure both operations are atomic
    public Member createMember(Member member) {
        // Ensure createdAt is set before insertion, especially if Member object
        // is created from a request body (which might use default constructor)
        if (member.getCreatedAt() == null) {
            member.setCreatedAt(LocalDateTime.now());
        }
        memberMapper.insert(member);

        // Create initial reward points for the new member
        // Assuming 0 initial points, can be changed
        rewardPointsService.createRewardPoints(new RewardPoints(member.getMemberId(), 0.0));

        return member;
    }

    public Optional<Member> getMemberById(Long memberId) {
        return Optional.ofNullable(memberMapper.findByMemberId(memberId));
    }

    public List<Member> getAllMembers() {
        return memberMapper.findAll();
    }

    public Member updateMember(Long memberId, Member memberDetails) {
        Member existingMember = memberMapper.findByMemberId(memberId);
        if (existingMember == null) {
            throw new IllegalArgumentException("Member not found with id " + memberId);
        }
        memberDetails.setMemberId(memberId); // Ensure the ID is set for update
        memberMapper.update(memberDetails);
        return memberDetails;
    }

    public void deleteMember(Long memberId) {
        memberMapper.delete(memberId);
    }
}