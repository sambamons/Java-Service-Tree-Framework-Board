package com.team1.board.common;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockFilterChain;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team1.board.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

/** REQ-SE-001 세션 기반 인증 필터. */
class LoginFilterTest extends IntegrationTestSupport {

    @Test
    void whitelist_passesWithoutLogin() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/member/join")).andExpect(status().isOk());
        mockMvc.perform(get("/member/login")).andExpect(status().isOk());
        mockMvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void protectedGet_redirectsToLoginWithRedirectURL() throws Exception {
        mockMvc.perform(get("/member/list"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/member/login?redirectURL=%2Fmember%2Flist"));
        mockMvc.perform(get("/board/write")).andExpect(status().isFound());
        mockMvc.perform(get("/board/1/edit")).andExpect(status().isFound());
    }

    @Test
    void publicBoardGet_passesWithoutLogin() throws Exception {
        LoginMember writer = createMember("writer", "w@test.com", "작성자");
        Long postId = createPost(writer, "제목", "내용");
        mockMvc.perform(get("/board")).andExpect(status().isOk());
        mockMvc.perform(get("/board/" + postId)).andExpect(status().isOk());
    }

    @Test
    void protectedPost_redirectsToLoginWithoutRedirectURL() throws Exception {
        String location = mockMvc.perform(post("/board/1/delete"))
                .andExpect(status().isFound())
                .andReturn().getResponse().getRedirectedUrl();
        assertThat(location).isEqualTo("/member/login");
    }

    /** 비정규화 경로는 공개 접두어로 시작해도 공개로 보지 않는다. MockMvc 는 경로를 정규화하지 않으므로 필터를 직접 호출한다. */
    @Test
    void unnormalizedPath_isNotTreatedAsPublic() throws Exception {
        for (String uri : new String[] {"/css/../member/list", "/css/x;/../member/list", "/css//../member/list"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            new LoginFilter().doFilter(request, response, chain);
            assertThat(chain.getRequest()).as(uri).isNull();
            assertThat(response.getRedirectedUrl()).as(uri).startsWith("/member/login");
        }
    }
}
