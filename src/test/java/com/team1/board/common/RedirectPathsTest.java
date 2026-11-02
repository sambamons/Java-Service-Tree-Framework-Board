package com.team1.board.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** 복귀 주소 검사(RedirectPaths.safeInternal) 직접 검증. */
class RedirectPathsTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"board", "https://evil.com", "//evil.com", "/\\evil.com", "/\t/evil.com", "/{x}"})
    void unsafe_root(String redirectURL) {
        assertThat(RedirectPaths.safeInternal(redirectURL)).isEqualTo("/");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "/board/write|/board/write",
            "/board?keyword=%ED%95%9C|/board?keyword=%ED%95%9C",
            "/board?keyword=한|/board?keyword=%ED%95%9C",
            "'/a b'|/a%20b"
    })
    void internal_kept(String redirectURL, String expected) {
        assertThat(RedirectPaths.safeInternal(redirectURL)).isEqualTo(expected);
    }
}
