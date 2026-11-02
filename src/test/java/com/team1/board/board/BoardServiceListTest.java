package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team1.board.member.MemberEntity;
import com.team1.board.member.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** A-02 특성화 테스트: 목록의 정렬·검색 종류 해석(알 수 없는 값의 기본값 포함)과 페이지 계산을 고정한다. */
class BoardServiceListTest {

    private BoardRepository boardRepository;
    private BoardService boardService;
    private Page<BoardEntity> onePost;

    @BeforeEach
    void setUp() {
        boardRepository = mock(BoardRepository.class);
        boardService = new BoardService(boardRepository, mock(CommentRepository.class), mock(MemberRepository.class));
        MemberEntity writer = mock(MemberEntity.class);
        when(writer.getMemberName()).thenReturn("김인후");
        onePost = new PageImpl<>(List.of(new BoardEntity(BoardType.FREE, "제목", "내용", writer)));
        when(boardRepository.findByBoardType(any(), any())).thenReturn(onePost);
        when(boardRepository.findByBoardTypeAndTitleContaining(any(), anyString(), any())).thenReturn(onePost);
        when(boardRepository.findByBoardTypeAndContentContaining(any(), anyString(), any())).thenReturn(onePost);
        when(boardRepository.findByBoardTypeAndWriter_MemberNameContaining(any(), anyString(), any())).thenReturn(onePost);
    }

    @Test
    void oldest_sortsAscendingById() {
        assertThat(sortOf("oldest")).isEqualTo(Sort.by(Sort.Direction.ASC, "id"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"latest", "OLDEST", "Oldest", "xyz", " oldest"})
    void anythingElse_sortsDescendingById(String sort) {
        assertThat(sortOf(sort)).isEqualTo(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Test
    void searchByContent() {
        boardService.list(1, "latest", "content", "k");
        verify(boardRepository).findByBoardTypeAndContentContaining(eq(BoardType.FREE), eq("k"), any());
    }

    @Test
    void searchByWriter() {
        boardService.list(1, "latest", "writer", "k");
        verify(boardRepository).findByBoardTypeAndWriter_MemberNameContaining(eq(BoardType.FREE), eq("k"), any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"title", "xyz", "CONTENT", "Writer"})
    void anythingElse_searchesTitle(String type) {
        boardService.list(1, "latest", type, "k");
        verify(boardRepository).findByBoardTypeAndTitleContaining(eq(BoardType.FREE), eq("k"), any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankKeyword_listsAllEvenWithType(String keyword) {
        boardService.list(1, "latest", "content", keyword);
        verify(boardRepository).findByBoardType(eq(BoardType.FREE), any());
        verify(boardRepository, never()).findByBoardTypeAndContentContaining(any(), anyString(), any());
    }

    @Test
    void keywordIsNotTrimmed() {
        boardService.list(1, "latest", "title", " k ");
        verify(boardRepository).findByBoardTypeAndTitleContaining(eq(BoardType.FREE), eq(" k "), any());
    }

    @Test
    void pageIsOneBasedAndClampedToFirst() {
        assertThat(pageableOf(3).getPageNumber()).isEqualTo(2);
        assertThat(pageableOf(0).getPageNumber()).isZero();
        assertThat(pageableOf(-5).getPageNumber()).isZero();
        assertThat(pageableOf(1).getPageSize()).isEqualTo(10);
    }

    @Test
    void mapsEntitiesToSummaries() {
        Page<BoardSummary> page = boardService.list(1, "latest", "title", "");
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("제목");
        assertThat(page.getContent().get(0).getWriterName()).isEqualTo("김인후");
    }

    private Sort sortOf(String sort) {
        return capturePageable(1, sort).getSort();
    }

    private Pageable pageableOf(int page) {
        return capturePageable(page, "latest");
    }

    private Pageable capturePageable(int page, String sort) {
        boardRepository = mock(BoardRepository.class);
        when(boardRepository.findByBoardType(any(), any())).thenReturn(onePost);
        boardService = new BoardService(boardRepository, mock(CommentRepository.class), mock(MemberRepository.class));
        boardService.list(page, sort, "title", "");
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(boardRepository).findByBoardType(eq(BoardType.FREE), captor.capture());
        return captor.getValue();
    }
}
