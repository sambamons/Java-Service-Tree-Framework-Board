package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team1.board.board.BoardRepository;
import com.team1.board.board.CommentRepository;
import com.team1.board.common.ForbiddenException;
import com.team1.board.common.LoginMember;
import com.team1.board.common.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** A-01 특성화 테스트: 회원 탈퇴의 본인 확인(존재 404 → 본인 403 순서, 메시지, 연쇄 삭제 순서)을 고정한다. */
class MemberServiceDeleteTest {

    private static final Long SELF_ID = 1000L;
    private static final Long OTHER_ID = 2000L;
    private static final Long MISSING_ID = 9999L;

    private MemberRepository memberRepository;
    private BoardRepository boardRepository;
    private CommentRepository commentRepository;
    private MemberService memberService;
    private final LoginMember self = new LoginMember(Long.valueOf(1000), "self", "s@test.com", "본인");

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        boardRepository = mock(BoardRepository.class);
        commentRepository = mock(CommentRepository.class);
        memberService = new MemberService(memberRepository, boardRepository, commentRepository,
                mock(PasswordEncoder.class));
        when(memberRepository.existsById(SELF_ID)).thenReturn(true);
        when(memberRepository.existsById(OTHER_ID)).thenReturn(true);
        when(memberRepository.existsById(MISSING_ID)).thenReturn(false);
    }

    @Test
    void delete_self_cascadesInOrder() {
        memberService.delete(Long.valueOf(1000), self);
        InOrder order = inOrder(commentRepository, boardRepository, memberRepository);
        order.verify(commentRepository).deleteByWriterId(SELF_ID);
        order.verify(commentRepository).deleteByBoardWriterId(SELF_ID);
        order.verify(boardRepository).deleteByWriterId(SELF_ID);
        order.verify(memberRepository).deleteById(SELF_ID);
    }

    @Test
    void delete_other_403_noDeletes() {
        assertThatThrownBy(() -> memberService.delete(OTHER_ID, self))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("본인만 삭제할 수 있습니다.");
        verifyNoDeletes();
    }

    @Test
    void delete_missing_404() {
        LoginMember missingSelf = new LoginMember(MISSING_ID, "gone", "g@test.com", "탈퇴함");
        assertThatThrownBy(() -> memberService.delete(MISSING_ID, missingSelf))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("회원이 없습니다: 9999");
        verifyNoDeletes();
    }

    /** 존재 확인이 본인 확인보다 먼저다: 남의 없는 id 는 403 이 아니라 404. */
    @Test
    void delete_missingOther_404Not403() {
        assertThatThrownBy(() -> memberService.delete(MISSING_ID, self))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("회원이 없습니다: 9999");
        verifyNoDeletes();
    }

    private void verifyNoDeletes() {
        verify(commentRepository, never()).deleteByWriterId(anyLong());
        verify(commentRepository, never()).deleteByBoardWriterId(anyLong());
        verify(boardRepository, never()).deleteByWriterId(anyLong());
        verify(memberRepository, never()).deleteById(any());
    }
}
