package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.team1.board.member.MemberEntity;
import com.team1.board.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

/** A-04 특성화 테스트: board_table 의 제목 컬럼 길이(100)를 고정한다. */
@DataJpaTest
class BoardEntityLengthTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BoardRepository boardRepository;

    MemberEntity writer;

    @BeforeEach
    void setUp() {
        writer = memberRepository.saveAndFlush(new MemberEntity("minho", "m@t.kr", "hash", "김민호"));
    }

    @Test
    void hundredCharacters_saved() {
        BoardEntity saved = boardRepository.saveAndFlush(new BoardEntity(BoardType.FREE, "가".repeat(100), "내용", writer));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void titleOver100_rejected() {
        assertThatThrownBy(() -> boardRepository.saveAndFlush(
                new BoardEntity(BoardType.FREE, "가".repeat(101), "내용", writer)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
