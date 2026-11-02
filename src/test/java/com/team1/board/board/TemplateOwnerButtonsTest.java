package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.common.LoginMember;
import org.junit.jupiter.api.Test;

/** R-01 특성화 테스트: 화면의 본인 전용 버튼(글·댓글 수정/삭제, 회원 탈퇴) 노출 조건을 고정한다. */
class TemplateOwnerButtonsTest extends IntegrationTestSupport {

    @Test
    void postButtons_onlyForWriter() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        LoginMember other = createMember("minho", "m@test.com", "김민호");
        Long postId = createPost(writer, "제목", "내용");
        String edit = "/board/" + postId + "/edit";
        String delete = "/board/" + postId + "/delete";

        assertThat(html("/board/" + postId, writer)).contains(edit).contains(delete);
        assertThat(html("/board/" + postId, other)).doesNotContain(edit).doesNotContain(delete);
        assertThat(html("/board/" + postId, null)).doesNotContain(edit).doesNotContain(delete);
    }

    @Test
    void commentButtons_onlyForCommentWriter() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        LoginMember other = createMember("minho", "m@test.com", "김민호");
        Long postId = createPost(writer, "제목", "내용");
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(writer)).param("content", "내 댓글"));
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(other)).param("content", "남 댓글"));
        Long mine = commentRepository.findAll().stream()
                .filter(c -> c.getContent().equals("내 댓글")).findFirst().orElseThrow().getId();
        Long theirs = commentRepository.findAll().stream()
                .filter(c -> c.getContent().equals("남 댓글")).findFirst().orElseThrow().getId();

        String asWriter = html("/board/" + postId, writer);
        assertThat(asWriter).contains("/comments/" + mine + "/edit").contains("/comments/" + mine + "/delete")
                .doesNotContain("/comments/" + theirs + "/edit").doesNotContain("/comments/" + theirs + "/delete");
        assertThat(html("/board/" + postId, null))
                .doesNotContain("/comments/" + mine + "/edit").doesNotContain("/comments/" + theirs + "/delete");
    }

    @Test
    void memberListWithdrawButton_onlyOnOwnRow() throws Exception {
        LoginMember me = createMember("inhu", "i@test.com", "김인후");
        LoginMember other = createMember("minho", "m@test.com", "김민호");
        assertThat(html("/member/list", me))
                .contains("/member/delete/" + me.getId())
                .doesNotContain("/member/delete/" + other.getId());
    }

    private String html(String url, LoginMember loginMember) throws Exception {
        var request = get(url);
        if (loginMember != null) {
            request.session(sessionOf(loginMember));
        }
        return mockMvc.perform(request).andReturn().getResponse().getContentAsString();
    }
}
