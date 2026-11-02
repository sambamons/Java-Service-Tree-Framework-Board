package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** 목록 정렬·검색 파라미터 해석(BoardSort·BoardSearchType). */
class BoardListParameterTest {

    @Test
    void sort_exactMatch() {
        assertThat(BoardSort.from("oldest")).isEqualTo(BoardSort.OLDEST);
        assertThat(BoardSort.from("latest")).isEqualTo(BoardSort.LATEST);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"OLDEST", " oldest", "xyz"})
    void sort_unknownIsLatest(String parameter) {
        assertThat(BoardSort.from(parameter)).isEqualTo(BoardSort.LATEST);
    }

    @Test
    void searchType_exactMatch() {
        assertThat(BoardSearchType.from("title")).isEqualTo(BoardSearchType.TITLE);
        assertThat(BoardSearchType.from("content")).isEqualTo(BoardSearchType.CONTENT);
        assertThat(BoardSearchType.from("writer")).isEqualTo(BoardSearchType.WRITER);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"CONTENT", "Writer", "xyz"})
    void searchType_unknownIsTitle(String parameter) {
        assertThat(BoardSearchType.from(parameter)).isEqualTo(BoardSearchType.TITLE);
    }
}
