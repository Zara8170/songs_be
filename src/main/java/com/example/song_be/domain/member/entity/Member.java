package com.example.song_be.domain.member.entity;

import com.example.song_be.domain.member.enums.MemberRole;
import com.example.song_be.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private String phone;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_role_list", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "role")
    @Builder.Default
    private List<MemberRole> memberRoleList = new ArrayList<>();

    public void addRole(MemberRole memberRole) {
        memberRoleList.add(memberRole);
    }

    /**
     * 소셜 맴버 엔티티 정적 팩토리 메서드
     *
     * @param email
     * @param encodedPw
     * @return 소셜 맴버 엔티티
     */
    public static Member fromSocialMember(String email, String encodedPw) {
        Member m = Member.builder()
                .email(email)
                .password(encodedPw)
                .build();
        m.addRole(MemberRole.USER);
        return m;
    }

//    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private List<SongLike> likedSongs = new ArrayList<>();
}