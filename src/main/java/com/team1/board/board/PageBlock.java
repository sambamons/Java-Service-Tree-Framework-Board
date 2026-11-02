package com.team1.board.board;

/**
 * 목록 하단 페이지 이동 영역(REQ-F-010). 번호는 현재 페이지가 속한 10개 묶음만 보이고,
 * « ‹ › » 는 처음·이전·다음·마지막 페이지로 간다. page 는 1부터 센다.
 */
public final class PageBlock {

    static final int BLOCK_SIZE = 10;

    private final int current;
    private final int start;
    private final int end;
    private final int last;

    private PageBlock(int current, int start, int end, int last) {
        this.current = current;
        this.start = start;
        this.end = end;
        this.last = last;
    }

    public static PageBlock of(int page, int totalPages) {
        int current = Math.max(page, 1);
        int last = Math.max(totalPages, 1);
        // 범위를 넘은 page 는 마지막 묶음을 보여 준다
        int base = Math.min(current, last);
        int start = (base - 1) / BLOCK_SIZE * BLOCK_SIZE + 1;
        int end = Math.min(start + BLOCK_SIZE - 1, last);
        return new PageBlock(current, start, end, last);
    }

    public int getCurrent() {
        return current;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLast() {
        return last;
    }

    public int getPrevPage() {
        return Math.min(current - 1, last);
    }

    public int getNextPage() {
        return current + 1;
    }

    public boolean isFirstPage() {
        return current <= 1;
    }

    public boolean isLastPage() {
        return current >= last;
    }
}
