package com.team1.board.board;

import com.team1.board.common.ForbiddenException;
import com.team1.board.common.LoginMember;
import com.team1.board.common.NotFoundException;
import com.team1.board.member.MemberEntity;
import com.team1.board.member.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 댓글(REQ-F-015). 수정·삭제는 작성자 본인만 한다. 반환값은 돌아갈 게시글 id 다. */
@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public CommentService(CommentRepository commentRepository, BoardRepository boardRepository,
                          MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    public List<CommentView> listByBoard(Long boardId) {
        return commentRepository.findByBoardIdOrderByIdAsc(boardId).stream()
                .map(CommentView::new).collect(Collectors.toList());
    }

    @Transactional
    public Long write(Long boardId, CommentForm form, LoginMember actor) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("게시글이 없습니다: " + boardId));
        MemberEntity writer = memberRepository.findById(actor.getId())
                .orElseThrow(() -> new NotFoundException("회원이 없습니다: " + actor.getId()));
        commentRepository.save(new CommentEntity(board, writer, form.getContent()));
        return boardId;
    }

    @Transactional
    public Long update(Long commentId, CommentForm form, LoginMember actor) {
        CommentEntity comment = findOwned(commentId, actor);
        comment.update(form.getContent());
        return comment.getBoard().getId();
    }

    @Transactional
    public Long delete(Long commentId, LoginMember actor) {
        CommentEntity comment = findOwned(commentId, actor);
        Long boardId = comment.getBoard().getId();
        commentRepository.delete(comment);
        return boardId;
    }

    /** 작성자 본인 댓글인지 확인하고 소속 게시글 id 를 돌려준다. */
    public Long boardIdOfOwned(Long commentId, LoginMember actor) {
        return findOwned(commentId, actor).getBoard().getId();
    }

    private CommentEntity findOwned(Long commentId, LoginMember actor) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글이 없습니다: " + commentId));
        if (!actor.isSelf(comment.getWriter().getId())) {
            throw new ForbiddenException("작성자만 수정·삭제할 수 있습니다.");
        }
        return comment;
    }
}
