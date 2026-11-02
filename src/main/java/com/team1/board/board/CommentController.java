package com.team1.board.board;

import com.team1.board.common.LoginMember;
import com.team1.board.common.SessionConst;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 댓글 작성·수정·삭제(REQ-F-015). 처리 후 게시글 상세로 돌아간다. */
@Controller
public class CommentController {

    private static final String COMMENT_ERROR = "commentError";

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/board/{boardId:\\d+}/comments")
    public String write(@PathVariable Long boardId,
                        @Valid @ModelAttribute CommentForm commentForm, BindingResult bindingResult,
                        @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(COMMENT_ERROR, firstError(bindingResult));
            return "redirect:/board/" + boardId;
        }
        return "redirect:/board/" + commentService.write(boardId, commentForm, loginMember);
    }

    @PostMapping("/comments/{commentId:\\d+}/edit")
    public String edit(@PathVariable Long commentId,
                       @Valid @ModelAttribute CommentForm commentForm, BindingResult bindingResult,
                       @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // 권한·존재 검사를 먼저 거친 뒤 오류를 돌려준다
            Long boardId = commentService.boardIdOfOwned(commentId, loginMember);
            redirectAttributes.addFlashAttribute(COMMENT_ERROR, firstError(bindingResult));
            return "redirect:/board/" + boardId;
        }
        return "redirect:/board/" + commentService.update(commentId, commentForm, loginMember);
    }

    @PostMapping("/comments/{commentId:\\d+}/delete")
    public String delete(@PathVariable Long commentId,
                         @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember) {
        return "redirect:/board/" + commentService.delete(commentId, loginMember);
    }

    private String firstError(BindingResult bindingResult) {
        return bindingResult.getAllErrors().get(0).getDefaultMessage();
    }
}
