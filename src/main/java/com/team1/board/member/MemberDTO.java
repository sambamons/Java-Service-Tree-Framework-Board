package com.team1.board.member;

/** 화면 전달용 회원 정보. 비밀번호를 담지 않는다(REQ-SE-003). */
public class MemberDTO {

    private final Long id;
    private final String memberId;
    private final String memberEmail;
    private final String memberName;

    private MemberDTO(Long id, String memberId, String memberEmail, String memberName) {
        this.id = id;
        this.memberId = memberId;
        this.memberEmail = memberEmail;
        this.memberName = memberName;
    }

    public static MemberDTO from(MemberEntity entity) {
        return new MemberDTO(entity.getId(), entity.getMemberId(), entity.getMemberEmail(), entity.getMemberName());
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
}
