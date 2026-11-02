package com.team1.board.member;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** 회원 테이블(REQ-DA-001). */
@Entity
@Table(name = "member_table")
public class MemberEntity {

    /** 아이디·이메일·이름 컬럼 길이. 가입 검증(MemberJoinForm)과 이메일 중복 확인이 같은 값을 쓴다. */
    public static final int MEMBER_ID_MAX_LENGTH = 20;
    public static final int EMAIL_MAX_LENGTH = 20;
    public static final int NAME_MAX_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true, length = MEMBER_ID_MAX_LENGTH)
    private String memberId;

    @Column(name = "member_email", nullable = false, unique = true, length = EMAIL_MAX_LENGTH)
    private String memberEmail;

    @Column(name = "member_password", nullable = false, length = 100)
    private String memberPassword;

    @Column(name = "member_name", nullable = false, length = NAME_MAX_LENGTH)
    private String memberName;

    protected MemberEntity() {
    }

    public MemberEntity(String memberId, String memberEmail, String memberPassword, String memberName) {
        this.memberId = memberId;
        this.memberEmail = memberEmail;
        this.memberPassword = memberPassword;
        this.memberName = memberName;
    }

    public Long getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public String getMemberPassword() {
        return memberPassword;
    }

    public String getMemberName() {
        return memberName;
    }
}
