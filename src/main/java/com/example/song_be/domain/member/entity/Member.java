package com.example.song_be.domain.member.entity;

import com.example.song_be.domain.like.entity.SongLike;
import com.example.song_be.domain.member.dto.JoinRequestDTO;
import com.example.song_be.domain.member.enums.MemberRole;
import com.example.song_be.domain.song.entity.Song;
import com.example.song_be.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@DynamicUpdate
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String nickname;
    private String phone;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_role_list", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "role")
    @Builder.Default
    private List<MemberRole> memberRoleList = new ArrayList<>();

    public void addRole(MemberRole memberRole) {
        memberRoleList.add(memberRole);
    }

    public void updateNickName(String nickName) {
        this.nickname = nickName;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

    // 일반 엔티티 정적 팩토리 메서드
    public static Member from(JoinRequestDTO request) {
        Member member = Member.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(request.getPassword())
                .phone(request.getPhone())
                .build();

        member.addRole(MemberRole.USER);
        return member;

    }

    /**
     * 소셜 맴버 엔티티 정적 팩토리 메서드
     *
     * @param email
     * @param tempPassword
     * @return 소셜 맴버 엔티티
     */
    public static Member fromSocialMember(String email, String tempPassword) {
        Member member = Member.builder()
                .email(email)
                .password(tempPassword)
                .nickname("소셜회원")
                .build();
        member.addRole(MemberRole.USER);
        return member;
    }

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SongLike> likedSongs = new ArrayList<>();

    public void likeSong(Song song) {
        boolean exists = likedSongs.stream()
                .anyMatch(like -> like.getSong().equals(song));

        if (exists) return;

        SongLike.of(this, song);
    }

    public void unlikeSong(Song song) {
        likedSongs.removeIf(like -> {
            boolean matched = like.getSong().equals(song);
            if (matched) song.getLikes().remove(like);
            return matched;
        });

    }
}
