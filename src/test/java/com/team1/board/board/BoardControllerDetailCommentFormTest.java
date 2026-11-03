package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.common.LoginMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/** A-01 특성화 테스트: 상세 화면이 언제나 빈 댓글 폼(commentForm)을 모델에 넣는 현재 동작을 고정한다. */
class BoardControllerDetailCommentFormTest extends IntegrationTestSupport {

    LoginMember writer;
    Long postId;

    @BeforeEach
    void setUp() {
        writer = createMember("inhu", "i@test.com", "김인후");
        postId = createPost(writer, "제목", "내용");
    }

    @Test
    void loggedIn_emptyCommentFormRendered() throws Exception {
        MvcResult result = mockMvc.perform(get("/board/" + postId).session(sessionOf(writer))).andReturn();
        assertEmptyCommentForm(result);
        assertThat(result.getResponse().getContentAsString())
                .containsPattern("<textarea[^>]*aria-label=\"댓글 내용\"[^>]*></textarea>");
    }

    @Test
    void anonymous_commentFormInModelButNotRendered() throws Exception {
        MvcResult result = mockMvc.perform(get("/board/" + postId)).andReturn();
        assertEmptyCommentForm(result);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("aria-label=\"댓글 내용\"");
    }

    @Test
    void afterCommentError_commentFormStillEmpty() throws Exception {
        MvcResult error = mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(writer))
                .param("content", " ")).andReturn();
        MvcResult result = mockMvc.perform(get("/board/" + postId).session(sessionOf(writer))
                .flashAttrs(error.getFlashMap())).andReturn();
        assertEmptyCommentForm(result);
        assertThat(result.getModelAndView().getModel().get("commentError")).isEqualTo("댓글을 입력하세요.");
    }

    private void assertEmptyCommentForm(MvcResult result) {
        Object commentForm = result.getModelAndView().getModel().get("commentForm");
        assertThat(commentForm).isInstanceOf(CommentForm.class);
        assertThat(((CommentForm) commentForm).getContent()).isNull();
    }
}
