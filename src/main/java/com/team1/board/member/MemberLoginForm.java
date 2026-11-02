package com.team1.board.member;

import javax.validation.constraints.NotBlank;

/** 로그인 입력(REQ-F-003, REQ-NF-001). */
public class MemberLoginForm {

    @NotBlank(message = "이메일을 입력하세요.")
    private String memberEmail;

    @NotBlank(message = "비밀번호를 입력하세요.")
    private String memberPassword;

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
}
