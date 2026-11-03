package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;

/** A-04 특성화 테스트: 댓글 최대 길이(500)와 메시지를 고정한다. */
class CommentFormLengthTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void fiveHundredCharacters_pass() {
        assertThat(validator.validate(form("가".repeat(500)))).isEmpty();
    }

    @Test
    void contentOver500_rejectedWithMessage() {
        assertThat(messages(form("가".repeat(501)))).containsExactly("댓글은 500자 이내여야 합니다.");
    }

    private CommentForm form(String content) {
        CommentForm form = new CommentForm();
        form.setContent(content);
        return form;
    }

    private Set<String> messages(CommentForm form) {
        return validator.validate(form).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }
}
