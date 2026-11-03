package com.team1.board.board;

import com.team1.board.common.LoginMember;
import com.team1.board.common.SessionConst;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

/** 자유게시판 화면(REQ-F-010~014). */
@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    public BoardController(BoardService boardService, CommentService commentService) {
        this.boardService = boardService;
        this.commentService = commentService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(defaultValue = "title") String type,
                       @RequestParam(defaultValue = "") String keyword,
                       Model model) {
        Page<BoardSummary> posts = boardService.list(page, sort, type, keyword);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts.getNumber() + 1);
        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("pageBlock", PageBlock.of(posts.getNumber() + 1, posts.getTotalPages()));
        model.addAttribute("sort", sort);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        return "board/list";
    }

    @GetMapping("/{id:\\d+}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", boardService.get(id));
        model.addAttribute("comments", commentService.listByBoard(id));
        model.addAttribute("commentForm", new CommentForm());
        return "board/detail";
    }

    @GetMapping("/write")
    public String writeForm(@ModelAttribute("boardForm") BoardForm boardForm, Model model) {
        return writeFormView(model);
    }

    @PostMapping("/write")
    public String write(@Valid @ModelAttribute("boardForm") BoardForm boardForm, BindingResult bindingResult,
                        @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember, Model model) {
        if (bindingResult.hasErrors()) {
            return writeFormView(model);
        }
        return "redirect:/board/" + boardService.write(boardForm, loginMember);
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editForm(@PathVariable Long id,
                           @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember, Model model) {
        model.addAttribute("boardForm", boardService.getForEdit(id, loginMember));
        return editFormView(model, id);
    }

    @PostMapping("/{id:\\d+}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("boardForm") BoardForm boardForm, BindingResult bindingResult,
                       @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember, Model model) {
        if (bindingResult.hasErrors()) {
            boardService.getForEdit(id, loginMember); // 남의 글이면 검증 오류 화면 대신 403
            return editFormView(model, id);
        }
        boardService.update(id, boardForm, loginMember);
        return "redirect:/board/" + id;
    }

    @PostMapping("/{id:\\d+}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember) {
        boardService.delete(id, loginMember);
        return "redirect:/board";
    }

    /** 글쓰기 폼 화면(board/form). */
    private String writeFormView(Model model) {
        model.addAttribute("mode", "write");
        return "board/form";
    }

    /** 글 수정 폼 화면(board/form). 저장 경로에 쓸 글 번호를 함께 넘긴다. */
    private String editFormView(Model model, Long postId) {
        model.addAttribute("mode", "edit");
        model.addAttribute("postId", postId);
        return "board/form";
    }
}
