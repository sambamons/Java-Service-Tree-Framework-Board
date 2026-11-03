package com.team1.board.board;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 게시글 작성·수정 입력(REQ-F-014, REQ-NF-001). */
public class BoardForm {

    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = BoardEntity.TITLE_MAX_LENGTH, message = "제목은 100자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    @Size(max = 10000, message = "내용은 10000자 이내여야 합니다.")
    private String content;

    public BoardForm() {
    }

    public BoardForm(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
