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

/** A-04 특성화 테스트: comment_table 의 내용 컬럼 길이(500)를 고정한다. */
@DataJpaTest
class CommentEntityLengthTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    CommentRepository commentRepository;

    MemberEntity writer;
    BoardEntity board;

    @BeforeEach
    void setUp() {
        writer = memberRepository.saveAndFlush(new MemberEntity("minho", "m@t.kr", "hash", "김민호"));
        board = boardRepository.saveAndFlush(new BoardEntity(BoardType.FREE, "제목", "내용", writer));
    }

    @Test
    void fiveHundredCharacters_saved() {
        CommentEntity saved = commentRepository.saveAndFlush(new CommentEntity(board, writer, "가".repeat(500)));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void contentOver500_rejected() {
        assertThatThrownBy(() -> commentRepository.saveAndFlush(new CommentEntity(board, writer, "가".repeat(501))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
