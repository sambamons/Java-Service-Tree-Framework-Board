package com.team1.board.board;

/** 목록 정렬(REQ-F-011). 쿼리 파라미터 sort 값과 정확히 같을 때만 그 정렬, 아니면 최신순이다. */
public enum BoardSort {
    LATEST("latest"),
    OLDEST("oldest");

    private final String parameter;

    BoardSort(String parameter) {
        this.parameter = parameter;
    }

    public static BoardSort from(String parameter) {
        return OLDEST.parameter.equals(parameter) ? OLDEST : LATEST;
    }
}
