package com.team1.board.board;

/** 목록 검색 대상(REQ-F-012). 쿼리 파라미터 type 값과 정확히 같을 때만 그 대상, 아니면 제목이다. */
public enum BoardSearchType {
    TITLE("title"),
    CONTENT("content"),
    WRITER("writer");

    private final String parameter;

    BoardSearchType(String parameter) {
        this.parameter = parameter;
    }

    public static BoardSearchType from(String parameter) {
        for (BoardSearchType type : values()) {
            if (type.parameter.equals(parameter)) {
                return type;
            }
        }
        return TITLE;
    }
}
