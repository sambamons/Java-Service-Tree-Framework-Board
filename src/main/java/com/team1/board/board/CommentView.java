package com.team1.board.board;

import java.time.LocalDateTime;

/** 상세 화면의 댓글 한 건(REQ-F-015). */
public class CommentView {

    private final Long id;
    private final String content;
    private final Long writerId;
    private final String writerName;
    private final LocalDateTime createdAt;

    CommentView(CommentEntity comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.writerId = comment.getWriter().getId();
        this.writerName = comment.getWriter().getMemberName();
        this.createdAt = comment.getCreatedAt();
    }

    public Long getId() {
        return id;
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
}
