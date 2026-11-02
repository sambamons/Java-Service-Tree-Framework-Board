package com.team1.board.common;

import java.io.Serializable;

/** 세션 loginInfo 에 저장하는 로그인 정보. 비밀번호를 담지 않는다(REQ-SE-003). */
public class LoginMember implements Serializable {

    private final Long id;
    private final String memberId;
    private final String memberEmail;
    private final String memberName;

    public LoginMember(Long id, String memberId, String memberEmail, String memberName) {
        this.id = id;
        this.memberId = memberId;
        this.memberEmail = memberEmail;
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

    public String getMemberName() {
        return memberName;
    }

    /** 이 회원 번호(PK)가 로그인한 본인인가. 서비스의 작성자·본인 확인(REQ-SE-004)이 이것으로 판단한다. */
    public boolean isSelf(Long ownerId) {
        return id.equals(ownerId);
    }
}
