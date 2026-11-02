package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** 페이지 번호 10개 묶음과 « ‹ › » 이동 계산. */
class PageBlockTest {

    @ParameterizedTest(name = "page={0}, totalPages={1} → {2}~{3}")
    @CsvSource({
            // page, totalPages, start, end, last, prev, next, firstPage, lastPage
            "1,   13, 1,  10, 13, 0,  2,   true,  false",
            "7,   13, 1,  10, 13, 6,  8,   false, false",
            "10,  13, 1,  10, 13, 9,  11,  false, false",
            "11,  13, 11, 13, 13, 10, 12,  false, false",
            "13,  13, 11, 13, 13, 12, 14,  false, true",
            "20,  20, 11, 20, 20, 19, 21,  false, true",
            "21,  25, 21, 25, 25, 20, 22,  false, false",
            "0,   13, 1,  10, 13, 0,  2,   true,  false",
            "-5,  13, 1,  10, 13, 0,  2,   true,  false",
            "99,  13, 11, 13, 13, 13, 100, false, true",
            "1,   0,  1,  1,  1,  0,  2,   true,  true",
            "1,   1,  1,  1,  1,  0,  2,   true,  true",
            "10,  10, 1,  10, 10, 9,  11,  false, true",
            "11,  11, 11, 11, 11, 10, 12,  false, true",
            "1,   11, 1,  10, 11, 0,  2,   true,  false"
    })
    void computesBlockAndMoves(int page, int totalPages, int start, int end, int last,
                               int prev, int next, boolean firstPage, boolean lastPage) {
        PageBlock block = PageBlock.of(page, totalPages);

        assertThat(block.getStart()).isEqualTo(start);
        assertThat(block.getEnd()).isEqualTo(end);
        assertThat(block.getLast()).isEqualTo(last);
        // 비활성 칸(처음 페이지의 ‹, 마지막 페이지의 ›)의 이동 값은 화면에서 쓰지 않으므로 검사하지 않는다
        if (!firstPage) {
            assertThat(block.getPrevPage()).isEqualTo(prev);
        }
        if (!lastPage) {
            assertThat(block.getNextPage()).isEqualTo(next);
        }
        assertThat(block.isFirstPage()).isEqualTo(firstPage);
        assertThat(block.isLastPage()).isEqualTo(lastPage);
    }
}
