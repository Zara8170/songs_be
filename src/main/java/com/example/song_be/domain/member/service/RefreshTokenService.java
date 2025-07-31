package com.example.song_be.domain.member.service;

public interface RefreshTokenService {
    
    /**
     * Refresh Token으로 새로운 Access Token 발급
     * 
     * @param accessToken 만료된 Access Token
     * @return 새로운 Access Token과 관련 정보
     */
    String getRefreshToken(Long memberId);
    
    /**
     * Refresh Token 저장 (Redis + DB)
     * 
     * @param memberId 회원 ID
     * @param refreshToken Refresh Token
     * @param expiry 만료 시간 (밀리초)
     */
    void saveRefreshToken(Long memberId, String refreshToken, long expiry);
    
    /**
     * Refresh Token 삭제
     * 
     * @param memberId 회원 ID
     */
    void deleteRefreshToken(Long memberId);
    
    /**
     * Refresh Token 유효성 검증
     * 
     * @param refreshToken Refresh Token
     * @return 유효 여부
     */
    boolean validateRefreshToken(String refreshToken);
    
    /**
     * Refresh Token 만료 임박 여부 확인
     * 
     * @param refreshToken Refresh Token
     * @param minutes 임계 시간 (분)
     * @return 만료 임박 여부
     */
    boolean willExpireSoon(String refreshToken, int minutes);
} 