package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

/** REQ-DA-001: 아이디·이메일은 DB 유일 제약을 갖는다. */
@DataJpaTest
class MemberEntitySchemaTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void duplicateEmail_rejectedByDatabase() {
        memberRepository.saveAndFlush(new MemberEntity("a", "same@test.com", "hash", "이름"));
        assertThatThrownBy(() -> memberRepository.saveAndFlush(new MemberEntity("b", "same@test.com", "hash", "이름")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void duplicateMemberId_rejectedByDatabase() {
        memberRepository.saveAndFlush(new MemberEntity("same", "a@test.com", "hash", "이름"));
        assertThatThrownBy(() -> memberRepository.saveAndFlush(new MemberEntity("same", "b@test.com", "hash", "이름")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
