package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

/** A-03 특성화 테스트: member_table 의 아이디·이메일·이름 컬럼 길이(20)를 고정한다. */
@DataJpaTest
class MemberEntityLengthTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void twentyCharacters_saved() {
        MemberEntity saved = memberRepository.saveAndFlush(
                new MemberEntity("a".repeat(20), MemberJoinFormLengthTest.email(20), "hash", "가".repeat(20)));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void memberIdOver20_rejected() {
        assertThatThrownBy(() -> memberRepository.saveAndFlush(
                new MemberEntity("a".repeat(21), "a@t.kr", "hash", "이름")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void emailOver20_rejected() {
        assertThatThrownBy(() -> memberRepository.saveAndFlush(
                new MemberEntity("minho", MemberJoinFormLengthTest.email(21), "hash", "이름")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void nameOver20_rejected() {
        assertThatThrownBy(() -> memberRepository.saveAndFlush(
                new MemberEntity("minho", "a@t.kr", "hash", "가".repeat(21))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
