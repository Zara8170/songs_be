package com.example.song_be.domain.member.entity;

import com.example.song_be.domain.member.enums.MemberRole;
import com.example.song_be.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@DynamicUpdate
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tempId;

    @Setter
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role = MemberRole.ACTIVE;

    public void setRole(MemberRole memberRole) {
        if (memberRole == null) {
            throw new IllegalArgumentException("memberRole must not be null");
        }

        if(this.role != memberRole) {
            this.role = memberRole;
        }
    }
}
