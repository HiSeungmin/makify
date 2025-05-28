package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.MemberStatus;
import com.xladmt.makify.common.constant.Role;
import com.xladmt.makify.common.constant.YN;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 25)
    private String loginId;

    @Column(name = "password", nullable = false, length = 30)
    private String password;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate birthDate; // 생년월일

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVE; // 상태

    private Integer deAmt; // 예치금
    private Integer cpAmt; // 상금

    private LocalDateTime joinDate; // 가입일

    // 생성 메서드
    public static Member create(String loginId, String password, Role role, String name, String email,
                                LocalDate birthDate, String phoneNumber) {
        Member member = new Member();
        member.name         = name;
        member.role         = role;
        member.email        = email;
        member.loginId      = loginId;
        member.password     = password;
        member.birthDate    = birthDate;
        member.phoneNumber  = phoneNumber;
        return member;
    }

    // 수정 메서드
    public void update(String name, String email, String phoneNumber, Role role) {
        this.name               = StringUtils.isNotBlank(name) ? name : this.name;
        this.role               = Objects.nonNull(role) ? role : this.role;
        this.email              = Objects.nonNull(email) ? email : this.email;
        this.phoneNumber        = StringUtils.isNotBlank(phoneNumber) ? phoneNumber : this.phoneNumber;
    }

    // 비밀번호 재설정
    public void resetPassword(String password){
        this.password = password;
    }

    // 삭제 메서드
    public void delete() {
        this.status = MemberStatus.INACTIVE;
    }

    // 탈퇴 메서드
    public void withDraw() {
        this.status = MemberStatus.WITHDRAW;
    }


}
