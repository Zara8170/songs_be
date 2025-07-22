package com.example.song_be.domain.member.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data

@Schema(description = "로그인 성공 시 응답하는 회원 정보가 담기 dto")
public class LoginResponseDTO {

    private String email;
    private String nickname;
    private String phone;
    private List<String> roles;
    private String accessToken;
}