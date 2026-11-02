package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;

/** A-03 특성화 테스트: 가입 입력의 아이디·이메일·이름 최대 길이(20)와 메시지를 고정한다. */
class MemberJoinFormLengthTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void twentyCharacters_pass() {
        assertThat(validator.validate(form("a".repeat(20), email(20), "가".repeat(20)))).isEmpty();
    }

    @Test
    void memberIdOver20_rejectedWithMessage() {
        assertThat(messages(form("a".repeat(21), email(20), "이름"))).containsExactly("아이디는 20자 이내여야 합니다.");
    }

    @Test
    void emailOver20_rejectedWithMessage() {
        assertThat(messages(form("minho", email(21), "이름"))).containsExactly("이메일은 20자 이내여야 합니다.");
    }

    @Test
    void nameOver20_rejectedWithMessage() {
        assertThat(messages(form("minho", email(20), "가".repeat(21)))).containsExactly("이름은 20자 이내여야 합니다.");
    }

    /** 형식이 올바른 지정 길이의 이메일. */
    static String email(int length) {
        String domain = "@t.kr";
        return "a".repeat(length - domain.length()) + domain;
    }

    private Set<String> messages(MemberJoinForm form) {
        return validator.validate(form).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    private MemberJoinForm form(String memberId, String email, String name) {
        MemberJoinForm form = new MemberJoinForm();
        form.setMemberId(memberId);
        form.setMemberEmail(email);
        form.setMemberPassword("pass1234");
        form.setMemberName(name);
        return form;
    }
}
