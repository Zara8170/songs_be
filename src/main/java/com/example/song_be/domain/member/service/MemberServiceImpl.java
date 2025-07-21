package com.example.song_be.domain.member.service;

import com.example.song_be.domain.member.entity.Member;
import com.example.song_be.domain.member.enums.MemberRole;
import com.example.song_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService{

    private static final int INACTIVE_DAYS = 14;

    private final MemberRepository memberRepository;


    @Override
    @Transactional
    public Member upsertAndTouch(String tempId) {
        Member member = memberRepository.findByTempId(tempId)
                .orElseGet(() -> memberRepository.save(
                        Member.builder().tempId(tempId).build()));

        member.setLastActiveAt(LocalDateTime.now());
        member.setRole(MemberRole.ACTIVE);
        return memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findInactiveSince(LocalDateTime cutoff) {
        return memberRepository.findByLastActiveAtBefore(cutoff);
    }

    @Override
    @Transactional
    public int deactivateInactiveMembers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(INACTIVE_DAYS);
        List<Member> inactive = memberRepository.findByLastActiveAtBefore(cutoff);

        inactive.forEach(m -> m.setRole(MemberRole.INACTIVE));
        return inactive.size();
    }
}
