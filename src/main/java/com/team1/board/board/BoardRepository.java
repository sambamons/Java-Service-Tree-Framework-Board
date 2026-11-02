package com.team1.board.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    @EntityGraph(attributePaths = "writer")
    Page<BoardEntity> findByBoardType(BoardType boardType, Pageable pageable);

    @EntityGraph(attributePaths = "writer")
    Page<BoardEntity> findByBoardTypeAndTitleContaining(BoardType boardType, String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "writer")
    Page<BoardEntity> findByBoardTypeAndContentContaining(BoardType boardType, String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "writer")
    Page<BoardEntity> findByBoardTypeAndWriter_MemberNameContaining(BoardType boardType, String keyword,
                                                                     Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from BoardEntity b where b.writer.id = :memberId")
    void deleteByWriterId(@Param("memberId") Long memberId);
}
