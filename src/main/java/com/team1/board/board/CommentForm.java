package com.team1.board.board;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 댓글 작성·수정 입력(REQ-F-015, REQ-NF-001). */
public class CommentForm {

    @NotBlank(message = "댓글을 입력하세요.")
    @Size(max = CommentEntity.CONTENT_MAX_LENGTH, message = "댓글은 500자 이내여야 합니다.")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
