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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

/** A-05 특성화 테스트: 글 폼 화면(board/form)의 mode·postId 모델 설정과 403 순서를 고정한다. */
class BoardControllerFormViewTest {

    private BoardService boardService;
    private BoardController controller;
    private ExtendedModelMap model;
    private final LoginMember owner = new LoginMember(1000L, "o", "o@test.com", "주인");

    @BeforeEach
    void setUp() {
        boardService = mock(BoardService.class);
        controller = new BoardController(boardService, mock(CommentService.class));
        model = new ExtendedModelMap();
    }

    @Test
    void writeForm_setsWriteMode() {
        assertThat(controller.writeForm(new BoardForm(), model)).isEqualTo("board/form");
        assertThat(model.get("mode")).isEqualTo("write");
        assertThat(model.containsAttribute("postId")).isFalse();
    }

    @Test
    void write_validationError_rendersWriteFormWithoutSaving() {
        BoardForm form = new BoardForm("", "내용");
        BindingResult errors = errorsOf(form);
        assertThat(controller.write(form, errors, owner, model)).isEqualTo("board/form");
        assertThat(model.get("mode")).isEqualTo("write");
        assertThat(model.containsAttribute("postId")).isFalse();
        verify(boardService, never()).write(any(), any());
    }

    @Test
    void write_success_redirectsToNewPost() {
        BoardForm form = new BoardForm("제목", "내용");
        when(boardService.write(form, owner)).thenReturn(7L);
        assertThat(controller.write(form, new BeanPropertyBindingResult(form, "boardForm"), owner, model))
                .isEqualTo("redirect:/board/7");
    }

    @Test
    void editForm_setsEditModeAndPostId() {
        BoardForm loaded = new BoardForm("t", "c");
        when(boardService.getForEdit(5L, owner)).thenReturn(loaded);
        assertThat(controller.editForm(5L, owner, model)).isEqualTo("board/form");
        assertThat(model.get("boardForm")).isSameAs(loaded);
        assertThat(model.get("mode")).isEqualTo("edit");
        assertThat(model.get("postId")).isEqualTo(5L);
    }

    @Test
    void editForm_forbidden_propagatesBeforeModelSet() {
        when(boardService.getForEdit(5L, owner)).thenThrow(new ForbiddenException("작성자만 수정·삭제할 수 있습니다."));
        assertThatThrownBy(() -> controller.editForm(5L, owner, model)).isExactlyInstanceOf(ForbiddenException.class);
        assertThat(model.containsAttribute("mode")).isFalse();
    }

    @Test
    void edit_validationError_checksOwnerThenRendersEditForm() {
        BoardForm form = new BoardForm("", "내용");
        assertThat(controller.edit(5L, form, errorsOf(form), owner, model)).isEqualTo("board/form");
        verify(boardService).getForEdit(5L, owner);
        verify(boardService, never()).update(any(), any(), any());
        assertThat(model.get("mode")).isEqualTo("edit");
        assertThat(model.get("postId")).isEqualTo(5L);
    }

    @Test
    void edit_validationError_byOther_forbiddenBeforeModelSet() {
        BoardForm form = new BoardForm("", "내용");
        when(boardService.getForEdit(5L, owner)).thenThrow(new ForbiddenException("작성자만 수정·삭제할 수 있습니다."));
        assertThatThrownBy(() -> controller.edit(5L, form, errorsOf(form), owner, model))
                .isExactlyInstanceOf(ForbiddenException.class);
        assertThat(model.containsAttribute("mode")).isFalse();
    }

    @Test
    void edit_success_updatesAndRedirects() {
        BoardForm form = new BoardForm("제목", "내용");
        assertThat(controller.edit(5L, form, new BeanPropertyBindingResult(form, "boardForm"), owner, model))
                .isEqualTo("redirect:/board/5");
        verify(boardService).update(5L, form, owner);
        verify(boardService, never()).getForEdit(any(), any());
    }

    private BindingResult errorsOf(BoardForm form) {
        BindingResult errors = new BeanPropertyBindingResult(form, "boardForm");
        errors.rejectValue("title", "NotBlank", "제목을 입력하세요.");
        return errors;
    }
}
