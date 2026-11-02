package com.team1.board.board;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @EntityGraph(attributePaths = "writer")
    List<CommentEntity> findByBoardIdOrderByIdAsc(Long boardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CommentEntity c where c.board.id = :boardId")
    void deleteByBoardId(@Param("boardId") Long boardId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CommentEntity c where c.writer.id = :memberId")
    void deleteByWriterId(@Param("memberId") Long memberId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CommentEntity c where c.board.id in "
            + "(select b.id from BoardEntity b where b.writer.id = :memberId)")
    void deleteByBoardWriterId(@Param("memberId") Long memberId);
}
