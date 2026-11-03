package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.common.LoginMember;
import com.team1.board.common.SessionConst;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

/** A-03 특성화 테스트: 로그인·로그아웃·탈퇴의 세션 무효화를 실제 진입 경로(필터 포함)로 고정한다. */
class MemberControllerSessionTest extends IntegrationTestSupport {

    @Test
    void logout_withoutSession_redirectsAndCreatesNoSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/member/logout"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/member/login")).andReturn();
        assertThat(result.getRequest().getSession(false)).isNull();
    }

    @Test
    void login_withoutPreviousSession_createsLoginSession() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        MvcResult result = mockMvc.perform(post("/member/login")
                        .param("memberEmail", "m@test.com").param("memberPassword", PASSWORD))
                .andExpect(redirectedUrl("/")).andReturn();
        HttpSession session = result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(SessionConst.LOGIN_INFO)).isInstanceOf(LoginMember.class);
    }

    @Test
    void login_withPreviousSession_replacesSession() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        MockHttpSession before = new MockHttpSession();
        before.setAttribute("x", "old");
        MvcResult result = mockMvc.perform(post("/member/login").session(before)
                        .param("memberEmail", "m@test.com").param("memberPassword", PASSWORD))
                .andExpect(redirectedUrl("/")).andReturn();
        HttpSession after = result.getRequest().getSession(false);
        assertThat(before.isInvalid()).isTrue();
        assertThat(after).isNotSameAs(before);
        assertThat(after.getAttribute("x")).isNull();
    }

    @Test
    void login_failure_keepsExistingSession() throws Exception {
        createMember("minho", "m@test.com", "김민호");
        MockHttpSession before = new MockHttpSession();
        mockMvc.perform(post("/member/login").session(before)
                        .param("memberEmail", "m@test.com").param("memberPassword", "wrong"))
                .andExpect(view().name("member/login"));
        assertThat(before.isInvalid()).isFalse();
    }

    @Test
    void delete_otherOrMissing_keepsSession() throws Exception {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        LoginMember other = createMember("inhu", "i@test.com", "김인후");
        MockHttpSession session = sessionOf(me);
        mockMvc.perform(post("/member/delete/" + other.getId()).session(session)).andExpect(status().isForbidden());
        mockMvc.perform(post("/member/delete/999999").session(session)).andExpect(status().isNotFound());
        assertThat(session.isInvalid()).isFalse();
        assertThat(session.getAttribute(SessionConst.LOGIN_INFO)).isSameAs(me);
    }

    @Test
    void delete_self_invalidatesSessionAndRedirectsHome() throws Exception {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        MockHttpSession session = sessionOf(me);
        mockMvc.perform(post("/member/delete/" + me.getId()).session(session)).andExpect(redirectedUrl("/"));
        assertThat(session.isInvalid()).isTrue();
    }
}
