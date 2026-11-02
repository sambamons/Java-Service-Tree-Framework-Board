package com.team1.board.board;

import java.time.LocalDateTime;

/** 목록 화면 한 줄(REQ-F-010). */
public class BoardSummary {

    private final Long id;
    private final String title;
    private final String writerName;
    private final LocalDateTime createdAt;

    BoardSummary(BoardEntity board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.writerName = board.getWriter().getMemberName();
        this.createdAt = board.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getWriterName() {
        return writerName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
