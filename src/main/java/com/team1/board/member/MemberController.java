package com.team1.board.member;

import com.team1.board.common.LoginMember;
import com.team1.board.common.RedirectPaths;
import com.team1.board.common.SessionConst;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

/** 회원 엔드포인트(REQ-IF-001). */
@Controller
@RequestMapping("/member")
public class MemberController {

    private static final String LOGIN_ERROR = "이메일 또는 비밀번호가 올바르지 않습니다.";
    private static final int BCRYPT_MAX_BYTES = 72;
    private static final Pattern EMAIL_FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/join")
    public String joinForm(@ModelAttribute("joinForm") MemberJoinForm joinForm) {
        return "member/join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute("joinForm") MemberJoinForm joinForm, BindingResult bindingResult) {
        // BCrypt 는 72바이트를 넘는 뒷부분을 버리므로 바이트 기준으로 막는다(한글은 글자당 3바이트)
        if (joinForm.getMemberPassword() != null
                && joinForm.getMemberPassword().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_BYTES) {
            bindingResult.rejectValue("memberPassword", "tooLong", "비밀번호가 너무 깁니다(영문 72자, 한글 24자 이내).");
        }
        if (bindingResult.hasErrors()) {
            return "member/join";
        }
        try {
            memberService.signUp(joinForm);
        } catch (DuplicateMemberException e) {
            bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            return "member/join";
        }
        return "redirect:/member/login";
    }

    @PostMapping(value = "/email-check", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String emailCheck(@RequestParam(defaultValue = "") String memberEmail) {
        String email = memberEmail.trim();
        if (email.length() > MemberEntity.EMAIL_MAX_LENGTH || !EMAIL_FORMAT.matcher(email).matches()) {
            return "NO";
        }
        return memberService.isEmailAvailable(email) ? "OK" : "NO";
    }

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") MemberLoginForm loginForm,
                            @RequestParam(required = false) String redirectURL, Model model) {
        model.addAttribute("redirectURL", redirectURL);
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") MemberLoginForm loginForm, BindingResult bindingResult,
                        @RequestParam(required = false) String redirectURL,
                        HttpServletRequest request, Model model) {
        model.addAttribute("redirectURL", redirectURL);
        if (bindingResult.hasErrors()) {
            return "member/login";
        }
        Optional<LoginMember> loginMember =
                memberService.login(loginForm.getMemberEmail(), loginForm.getMemberPassword());
        if (loginMember.isEmpty()) {
            model.addAttribute("loginError", LOGIN_ERROR);
            return "member/login";
        }
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate(); // 세션 고정 방지
        }
        request.getSession(true).setAttribute(SessionConst.LOGIN_INFO, loginMember.get());
        return "redirect:" + RedirectPaths.safeInternal(redirectURL);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/member/login";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("members", memberService.findAll());
        return "member/list";
    }

    @GetMapping("/{id:\\d+}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        return "member/read";
    }

    @PostMapping("/delete/{id:\\d+}")
    public String delete(@PathVariable Long id,
                         @SessionAttribute(SessionConst.LOGIN_INFO) LoginMember loginMember,
                         HttpServletRequest request) {
        memberService.delete(id, loginMember);
        request.getSession().invalidate();
        return "redirect:/";
    }
}
