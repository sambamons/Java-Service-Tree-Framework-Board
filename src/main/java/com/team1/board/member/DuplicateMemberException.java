package com.team1.board.member;

/** 아이디 또는 이메일 중복 가입(REQ-NF-002). field 는 오류를 표시할 폼 필드 이름이다. */
public class DuplicateMemberException extends RuntimeException {

    private final String field;

    public DuplicateMemberException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
