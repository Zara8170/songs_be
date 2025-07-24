package com.example.song_be.domain.member.entity;

import com.example.song_be.domain.like.entity.SongLike;
import com.example.song_be.domain.member.enums.MemberRole;
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
@ToString(exclude = "memberRoleList")
@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String phone;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_role_list", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "role")
    @Builder.Default
    private List<MemberRole> memberRoleList = new ArrayList<>();

    public void addRole(MemberRole memberRole) {
        memberRoleList.add(memberRole);
    }

    public static Member fromSocialMember(String email, String password) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.addRole(MemberRole.USER);
        return member;
    }

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SongLike> likedSongs = new ArrayList<>();
}