package com.team1.board.board;

import com.team1.board.common.ForbiddenException;
import com.team1.board.common.LoginMember;
import com.team1.board.common.NotFoundException;
import com.team1.board.member.MemberEntity;
import com.team1.board.member.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 자유게시판(REQ-F-010~014). */
@Service
@Transactional(readOnly = true)
public class BoardService {

    static final int PAGE_SIZE = 10;

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    public BoardService(BoardRepository boardRepository, CommentRepository commentRepository,
                        MemberRepository memberRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
    }

    /** 목록(REQ-F-010) · 정렬(REQ-F-011) · 검색(REQ-F-012). page 는 1부터 센다. */
    public Page<BoardSummary> list(int page, String sort, String type, String keyword) {
        Sort.Direction direction = BoardSort.from(sort) == BoardSort.OLDEST ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, PAGE_SIZE, Sort.by(direction, "id"));

        if (!StringUtils.hasText(keyword)) {
            return boardRepository.findByBoardType(BoardType.FREE, pageable).map(BoardSummary::new);
        }
        Page<BoardEntity> result = switch (BoardSearchType.from(type)) {
            case CONTENT -> boardRepository.findByBoardTypeAndContentContaining(BoardType.FREE, keyword, pageable);
            case WRITER -> boardRepository.findByBoardTypeAndWriter_MemberNameContaining(BoardType.FREE, keyword, pageable);
            case TITLE -> boardRepository.findByBoardTypeAndTitleContaining(BoardType.FREE, keyword, pageable);
        };
        return result.map(BoardSummary::new);
    }

    /** 상세(REQ-F-013). */
    public BoardDetail get(Long id) {
        return new BoardDetail(find(id));
    }

    /** 수정 폼 내용. 작성자 본인만 볼 수 있다(REQ-F-014). */
    public BoardForm getForEdit(Long id, LoginMember actor) {
        BoardEntity board = findOwned(id, actor);
        return new BoardForm(board.getTitle(), board.getContent());
    }

    @Transactional
    public Long write(BoardForm form, LoginMember actor) {
        MemberEntity writer = memberRepository.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("회원이 없습니다: " + actor.getId()));
        return boardRepository.save(new BoardEntity(BoardType.FREE, form.getTitle(), form.getContent(), writer))
                .getId();
    }

    @Transactional
    public void update(Long id, BoardForm form, LoginMember actor) {
        findOwned(id, actor).update(form.getTitle(), form.getContent());
    }

    /** 삭제. 댓글을 먼저 지운다. */
    @Transactional
    public void delete(Long id, LoginMember actor) {
        findOwned(id, actor);
        commentRepository.deleteByBoardId(id);
        boardRepository.deleteById(id);
    }

    private BoardEntity find(Long id) {
        return boardRepository.findById(id).orElseThrow(() -> new NotFoundException("게시글이 없습니다: " + id));
    }

    private BoardEntity findOwned(Long id, LoginMember actor) {
        BoardEntity board = find(id);
        if (!actor.isSelf(board.getWriter().getId())) {
            throw new ForbiddenException("작성자만 수정·삭제할 수 있습니다.");
        }
        return board;
    }
}
