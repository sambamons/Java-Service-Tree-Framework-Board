package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.common.LoginMember;
import com.team1.board.common.SessionConst;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

class MemberControllerTest extends IntegrationTestSupport {

    @Test
    void join_success_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/member/join")
                        .param("memberId", "minho").param("memberEmail", "m@test.com")
                        .param("memberPassword", PASSWORD).param("memberName", "김민호"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/member/login"));
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    void join_duplicateEmailOrId_showsFormError() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(post("/member/join")
                        .param("memberId", "other").param("memberEmail", "m@test.com")
                        .param("memberPassword", PASSWORD).param("memberName", "다른사람"))
                .andExpect(status().isOk()).andExpect(view().name("member/join"))
                .andExpect(model().attributeHasFieldErrors("joinForm", "memberEmail"));
        mockMvc.perform(post("/member/join")
                        .param("memberId", "minho").param("memberEmail", "x@test.com")
                        .param("memberPassword", PASSWORD).param("memberName", "다른사람"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("joinForm", "memberId"));
        assertThat(memberRepository.count()).isEqualTo(1);
    }

    @Test
    void join_invalidInput_showsErrors() throws Exception {
        mockMvc.perform(post("/member/join")
                        .param("memberId", "").param("memberEmail", "not-an-email")
                        .param("memberPassword", "").param("memberName", "가".repeat(21)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("joinForm",
                        "memberId", "memberEmail", "memberPassword", "memberName"));
        assertThat(memberRepository.count()).isZero();
    }

    @Test
    void emailCheck_withoutLogin_returnsOkOrNo() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(post("/member/email-check").param("memberEmail", "new@test.com"))
                .andExpect(status().isOk()).andExpect(content().string("OK"));
        mockMvc.perform(post("/member/email-check").param("memberEmail", "m@test.com"))
                .andExpect(content().string("NO"));
    }

    @Test
    void login_success_sessionHasNoPasswordAndNewSessionId() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        MockHttpSession before = new MockHttpSession();
        MvcResult result = mockMvc.perform(post("/member/login").session(before)
                        .param("memberEmail", "m@test.com").param("memberPassword", PASSWORD))
                .andExpect(status().isFound()).andExpect(redirectedUrl("/"))
                .andReturn();
        assertThat(before.isInvalid()).isTrue();
        Object loginInfo = result.getRequest().getSession().getAttribute(SessionConst.LOGIN_INFO);
        assertThat(loginInfo).isInstanceOf(LoginMember.class);
        assertThat(Arrays.stream(LoginMember.class.getDeclaredFields()).map(f -> f.getName().toLowerCase()))
                .noneMatch(name -> name.contains("password"));
    }

    @Test
    void login_redirectsToInternalRedirectURL() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(post("/member/login").param("memberEmail", "m@test.com")
                        .param("memberPassword", PASSWORD).param("redirectURL", "/board/write"))
                .andExpect(redirectedUrl("/board/write"));
    }

    @Test
    void login_rejectsExternalOrMalformedRedirectURL() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        for (String target : new String[] {"//evil.com", "/\\evil.com", "/\t/evil.com", "https://evil.com", "/{x}"}) {
            mockMvc.perform(post("/member/login").param("memberEmail", "m@test.com")
                            .param("memberPassword", PASSWORD).param("redirectURL", target))
                    .andExpect(redirectedUrl("/"));
        }
    }

    @Test
    void login_fail_showsGenericMessageWithoutUrlParam() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(post("/member/login").param("memberEmail", "m@test.com").param("memberPassword", "wrong"))
                .andExpect(status().isOk()).andExpect(view().name("member/login"))
                .andExpect(model().attribute("loginError", "이메일 또는 비밀번호가 올바르지 않습니다."));
        mockMvc.perform(post("/member/login").param("memberEmail", "none@test.com").param("memberPassword", "x"))
                .andExpect(model().attribute("loginError", "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void logout_invalidatesAndRedirects() throws Exception {
        MockHttpSession session = sessionOf(createMember("minho", "m@test.com", "김민호"));
        mockMvc.perform(post("/member/logout").session(session))
                .andExpect(redirectedUrl("/member/login"));
        assertThat(session.isInvalid()).isTrue();
        mockMvc.perform(get("/member/list")).andExpect(status().isFound());
    }

    @Test
    void list_loggedIn_showsMembers() throws Exception {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        createMember("inhu", "i@test.com", "김인후");
        mockMvc.perform(get("/member/list").session(sessionOf(me)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("i@test.com")));
    }

    @Test
    void detail_missingId_returns404View() throws Exception {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(get("/member/999999").session(sessionOf(me)))
                .andExpect(status().isNotFound()).andExpect(view().name("error/404"));
    }

    @Test
    void delete_getIsNotAllowed() throws Exception {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(get("/member/delete/" + me.getId()).session(sessionOf(me)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void delete_other_returns403() throws Exception {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        LoginMember other = createMember("inhu", "i@test.com", "김인후");
        mockMvc.perform(post("/member/delete/" + other.getId()).session(sessionOf(me)))
                .andExpect(status().isForbidden()).andExpect(view().name("error/403"));
        assertThat(memberRepository.existsById(other.getId())).isTrue();
    }

    @Test
    void delete_self_deletesAndInvalidates() throws Exception {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        MockHttpSession session = sessionOf(me);
        mockMvc.perform(post("/member/delete/" + me.getId()).session(session))
                .andExpect(redirectedUrl("/"));
        assertThat(memberRepository.existsById(me.getId())).isFalse();
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void index_showsLoginState() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(content().string(containsString("회원가입")))
                .andExpect(content().string(not(containsString("로그아웃"))));
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(get("/").session(sessionOf(me)))
                .andExpect(content().string(containsString("김민호")))
                .andExpect(content().string(containsString("m@test.com")))
                .andExpect(content().string(containsString("로그아웃")))
                .andExpect(content().string(containsString("/member/list")));
    }

    @Test
    void emailCheck_invalidFormatOrTooLong_returnsNo() throws Exception {
        mockMvc.perform(post("/member/email-check").param("memberEmail", "not-an-email"))
                .andExpect(content().string("NO"));
        mockMvc.perform(post("/member/email-check").param("memberEmail", "abcdefghijk@test.com1"))
                .andExpect(content().string("NO"));
    }

    /** BCrypt 72바이트 한계: 한글 25자(75바이트) 비밀번호는 거부한다. */
    @Test
    void join_passwordOver72Bytes_showsError() throws Exception {
        mockMvc.perform(post("/member/join")
                        .param("memberId", "minho").param("memberEmail", "m@test.com")
                        .param("memberPassword", "가".repeat(25)).param("memberName", "김민호"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("joinForm", "memberPassword"));
        assertThat(memberRepository.count()).isZero();
    }

    /** 필터가 넘긴 인코딩된 쿼리는 다시 인코딩하지 않는다. */
    @Test
    void login_keepsEncodedQueryInRedirectURL() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        mockMvc.perform(post("/member/login").param("memberEmail", "m@test.com")
                        .param("memberPassword", PASSWORD).param("redirectURL", "/board?keyword=%ED%95%9C"))
                .andExpect(redirectedUrl("/board?keyword=%ED%95%9C"));
    }
}
