//package com.example.song_be.domain.member.scheduler;
//
//import com.example.song_be.domain.member.service.MemberService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class MemberScheduler {
//
//    private final MemberService memberService;
//
//    @Async
//    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
//    public void checkInactiveMembers() {
//        int changed = memberService.deactivateInactiveMembers();
//        log.info("[Scheduler] INACTIVE 로 전환된 회원 = {}", changed);
//    }
//}
