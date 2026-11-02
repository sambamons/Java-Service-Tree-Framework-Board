package com.team1.board.board;

import java.time.LocalDateTime;

/** 상세 화면(REQ-F-013). */
public class BoardDetail {

    private final Long id;
    private final String title;
    private final String content;
    private final Long writerId;
    private final String writerName;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    BoardDetail(BoardEntity board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.writerId = board.getWriter().getId();
        this.writerName = board.getWriter().getMemberName();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Long getWriterId() {
        return writerId;
    }

    public String getWriterName() {
        return writerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
