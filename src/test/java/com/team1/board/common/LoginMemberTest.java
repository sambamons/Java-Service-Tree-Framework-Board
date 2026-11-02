package com.team1.board.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** 로그인한 본인 여부 판단(A-01 에서 세 서비스의 id 비교를 모은 곳). */
class LoginMemberTest {

    private final LoginMember member = new LoginMember(Long.valueOf(1000), "minho", "m@test.com", "김민호");

    @Test
    void isSelf_sameId() {
        assertThat(member.isSelf(1000L)).isTrue();
    }

    /** Long 캐시 밖 값의 다른 인스턴스도 같은 회원으로 본다(== 가 아니라 equals 비교). */
    @Test
    void isSelf_sameValueDifferentInstance() {
        assertThat(member.isSelf(Long.valueOf(1000))).isTrue();
    }

    @Test
    void isSelf_otherId() {
        assertThat(member.isSelf(2000L)).isFalse();
    }

    @Test
    void isSelf_nullArgument() {
        assertThat(member.isSelf(null)).isFalse();
    }
}
