package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team1.board.common.ForbiddenException;
import com.team1.board.common.LoginMember;
import com.team1.board.common.NotFoundException;
import com.team1.board.member.MemberEntity;
import com.team1.board.member.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** A-01 특성화 테스트: 댓글 수정·삭제의 작성자 확인(404 → 403 순서, 메시지, 반환 게시글 id)을 고정한다. */
class CommentServiceTest {

    private CommentRepository commentRepository;
    private CommentService commentService;
    private CommentEntity comment;
    private final LoginMember owner = new LoginMember(Long.valueOf(1000), "owner", "o@test.com", "주인");
    private final LoginMember other = new LoginMember(2000L, "other", "x@test.com", "남");

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        commentService = new CommentService(commentRepository, mock(BoardRepository.class), mock(MemberRepository.class));
        MemberEntity writer = mock(MemberEntity.class);
        when(writer.getId()).thenReturn(Long.valueOf(1000));
        BoardEntity board = mock(BoardEntity.class);
        when(board.getId()).thenReturn(10L);
        comment = new CommentEntity(board, writer, "원래 댓글");
        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());
    }

    @Test
    void update_owner_returnsBoardId_updatesContent() {
        CommentForm form = form("고친 댓글");
        assertThat(commentService.update(5L, form, owner)).isEqualTo(10L);
        assertThat(comment.getContent()).isEqualTo("고친 댓글");
    }

    @Test
    void update_other_403_contentUnchanged() {
        assertThatThrownBy(() -> commentService.update(5L, form("탈취"), other))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("작성자만 수정·삭제할 수 있습니다.");
        assertThat(comment.getContent()).isEqualTo("원래 댓글");
    }

    @Test
    void update_missing_404() {
        assertThatThrownBy(() -> commentService.update(99L, form("x"), other))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("댓글이 없습니다: 99");
    }

    @Test
    void delete_owner_deletesAndReturnsBoardId() {
        assertThat(commentService.delete(5L, owner)).isEqualTo(10L);
        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_other_403_noDelete() {
        assertThatThrownBy(() -> commentService.delete(5L, other))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("작성자만 수정·삭제할 수 있습니다.");
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void boardIdOfOwned_owner_other_missing() {
        assertThat(commentService.boardIdOfOwned(5L, owner)).isEqualTo(10L);
        assertThatThrownBy(() -> commentService.boardIdOfOwned(5L, other))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("작성자만 수정·삭제할 수 있습니다.");
        assertThatThrownBy(() -> commentService.boardIdOfOwned(99L, owner))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("댓글이 없습니다: 99");
    }

    private CommentForm form(String content) {
        CommentForm form = new CommentForm();
        form.setContent(content);
        return form;
    }
}
