package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;

/** A-01 특성화 테스트: 게시글 수정·삭제의 작성자 확인(404 → 403 순서, 메시지, 삭제 순서)을 고정한다. */
class BoardServiceTest {

    // Long 캐시(-128~127) 밖 값을 써서 == 비교로 바뀌면 실패하게 한다
    private static final Long OWNER_ID = 1000L;
    private static final Long OTHER_ID = 2000L;

    private BoardRepository boardRepository;
    private CommentRepository commentRepository;
    private BoardService boardService;
    private BoardEntity board;
    private final LoginMember owner = new LoginMember(Long.valueOf(1000), "owner", "o@test.com", "주인");
    private final LoginMember other = new LoginMember(OTHER_ID, "other", "x@test.com", "남");

    @BeforeEach
    void setUp() {
        boardRepository = mock(BoardRepository.class);
        commentRepository = mock(CommentRepository.class);
        boardService = new BoardService(boardRepository, commentRepository, mock(MemberRepository.class));
        MemberEntity writer = mock(MemberEntity.class);
        when(writer.getId()).thenReturn(Long.valueOf(1000));
        board = new BoardEntity(BoardType.FREE, "제목", "내용", writer);
        when(boardRepository.findById(10L)).thenReturn(Optional.of(board));
        when(boardRepository.findById(99L)).thenReturn(Optional.empty());
    }

    @Test
    void getForEdit_owner_returnsForm() {
        BoardForm form = boardService.getForEdit(10L, owner);
        assertThat(form.getTitle()).isEqualTo("제목");
        assertThat(form.getContent()).isEqualTo("내용");
    }

    @Test
    void getForEdit_other_403() {
        assertThatThrownBy(() -> boardService.getForEdit(10L, other))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("작성자만 수정·삭제할 수 있습니다.");
    }

    @Test
    void getForEdit_missing_404BeforeOwnerCheck() {
        assertThatThrownBy(() -> boardService.getForEdit(99L, other))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("게시글이 없습니다: 99");
    }

    @Test
    void update_owner_updatesEntity() {
        boardService.update(10L, new BoardForm("새 제목", "새 내용"), owner);
        assertThat(board.getTitle()).isEqualTo("새 제목");
        assertThat(board.getContent()).isEqualTo("새 내용");
    }

    @Test
    void update_other_403_entityUnchanged() {
        assertThatThrownBy(() -> boardService.update(10L, new BoardForm("탈취", "x"), other))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("작성자만 수정·삭제할 수 있습니다.");
        assertThat(board.getTitle()).isEqualTo("제목");
    }

    @Test
    void delete_owner_deletesCommentsThenBoard() {
        boardService.delete(10L, owner);
        InOrder order = inOrder(commentRepository, boardRepository);
        order.verify(commentRepository).deleteByBoardId(10L);
        order.verify(boardRepository).deleteById(10L);
    }

    @Test
    void delete_other_403_noDeletes() {
        assertThatThrownBy(() -> boardService.delete(10L, other))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("작성자만 수정·삭제할 수 있습니다.");
        verify(commentRepository, never()).deleteByBoardId(anyLong());
        verify(boardRepository, never()).deleteById(any());
    }

    @Test
    void delete_missing_404_noDeletes() {
        assertThatThrownBy(() -> boardService.delete(99L, owner))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("게시글이 없습니다: 99");
        verify(commentRepository, never()).deleteByBoardId(anyLong());
        verify(boardRepository, never()).deleteById(any());
    }

    @Test
    void ownerIdComparedByValueNotIdentity() {
        assertThat(OWNER_ID).isNotSameAs(owner.getId());
        BoardForm form = boardService.getForEdit(10L, new LoginMember(Long.valueOf(1000), "o", "o@test.com", "주인"));
        assertThat(form).isNotNull();
    }
}
