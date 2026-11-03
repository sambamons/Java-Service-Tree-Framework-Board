package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;

/** A-04 특성화 테스트: 게시글 제목 최대 길이(100)와 메시지를 고정한다. */
class BoardFormLengthTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void hundredCharacters_pass() {
        assertThat(validator.validate(new BoardForm("가".repeat(100), "내용"))).isEmpty();
    }

    @Test
    void titleOver100_rejectedWithMessage() {
        assertThat(messages(new BoardForm("가".repeat(101), "내용"))).containsExactly("제목은 100자 이내여야 합니다.");
    }

    private Set<String> messages(BoardForm form) {
        return validator.validate(form).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }
}
