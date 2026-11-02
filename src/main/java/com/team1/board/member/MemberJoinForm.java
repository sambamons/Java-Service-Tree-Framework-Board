package com.team1.board.member;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 회원가입 입력(REQ-F-001, REQ-NF-001). */
public class MemberJoinForm {

    @NotBlank(message = "아이디를 입력하세요.")
    @Size(max = MemberEntity.MEMBER_ID_MAX_LENGTH, message = "아이디는 20자 이내여야 합니다.")
    private String memberId;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = MemberEntity.EMAIL_MAX_LENGTH, message = "이메일은 20자 이내여야 합니다.")
    private String memberEmail;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Size(max = 72, message = "비밀번호는 72자 이내여야 합니다.")
    private String memberPassword;

    @NotBlank(message = "이름을 입력하세요.")
    @Size(max = MemberEntity.NAME_MAX_LENGTH, message = "이름은 20자 이내여야 합니다.")
    private String memberName;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public String getMemberPassword() {
        return memberPassword;
    }

    public void setMemberPassword(String memberPassword) {
        this.memberPassword = memberPassword;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }
}
