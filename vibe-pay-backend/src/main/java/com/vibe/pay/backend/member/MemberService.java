package com.vibe.pay.backend.member;

import com.vibe.pay.backend.rewardpoints.RewardPoints;
import com.vibe.pay.backend.rewardpoints.RewardPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private RewardPointsService rewardPointsService; // Inject RewardPointsService

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
        rewardPointsService.createRewardPoints(new RewardPoints(member.getId(), 0.0));

        return member;
    }

    public Optional<Member> getMemberById(Long id) {
        return Optional.ofNullable(memberMapper.findById(id));
    }

    public List<Member> getAllMembers() {
        return memberMapper.findAll();
    }

    public Member updateMember(Long id, Member memberDetails) {
        Member existingMember = memberMapper.findById(id);
        if (existingMember == null) {
            throw new RuntimeException("Member not found with id " + id);
        }
        memberDetails.setId(id); // Ensure the ID is set for update
        memberMapper.update(memberDetails);
        return memberDetails;
    }

    public void deleteMember(Long id) {
        memberMapper.delete(id);
    }
}