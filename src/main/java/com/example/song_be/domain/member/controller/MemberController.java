package com.example.song_be.domain.member.controller;

import com.example.song_be.domain.member.dto.MemberTrackRequest;
import com.example.song_be.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/track")
    public ResponseEntity<Void> track(@RequestBody MemberTrackRequest request) {
        memberService.upsertAndTouch(request.getTempId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<String>> inactive(
            @RequestParam(defaultValue = "14") int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<String> tempIds = memberService.findInactiveSince(cutoff)
                .stream()
                .map(m -> m.getTempId())
                .toList();
        return ResponseEntity.ok(tempIds);
    }
}
