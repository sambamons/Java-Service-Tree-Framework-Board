package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.common.LoginMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** REQ-F-015 댓글. */
class CommentControllerTest extends IntegrationTestSupport {

    LoginMember writer;
    LoginMember other;
    Long postId;

    @BeforeEach
    void setUp() {
        writer = createMember("inhu", "i@test.com", "김인후");
        other = createMember("minho", "m@test.com", "김민호");
        postId = createPost(writer, "제목", "내용");
    }

    @Test
    void write_withoutLogin_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/board/" + postId + "/comments").param("content", "댓글"))
                .andExpect(redirectedUrlPattern("/member/login*"));
        assertThat(commentRepository.count()).isZero();
    }

    @Test
    void write_success_showsOnDetail() throws Exception {
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(other)).param("content", "첫 댓글"))
                .andExpect(redirectedUrl("/board/" + postId));
        mockMvc.perform(get("/board/" + postId)).andExpect(content().string(containsString("첫 댓글")));
    }

    @Test
    void write_blankOrTooLong_flashesError() throws Exception {
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(other)).param("content", " "))
                .andExpect(redirectedUrl("/board/" + postId))
                .andExpect(flash().attributeExists("commentError"));
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(other)).param("content", "가".repeat(501)))
                .andExpect(flash().attributeExists("commentError"));
        assertThat(commentRepository.count()).isZero();
    }

    @Test
    void edit_byOther_403_byAuthor_updates() throws Exception {
        Long commentId = writeComment(other, "원래 댓글");
        mockMvc.perform(post("/comments/" + commentId + "/edit").session(sessionOf(writer)).param("content", "탈취"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/comments/" + commentId + "/edit").session(sessionOf(other)).param("content", "고친 댓글"))
                .andExpect(redirectedUrl("/board/" + postId));
        assertThat(commentRepository.findById(commentId).orElseThrow().getContent()).isEqualTo("고친 댓글");
    }

    @Test
    void delete_byOther_403_byAuthor_deletes() throws Exception {
        Long commentId = writeComment(other, "댓글");
        mockMvc.perform(post("/comments/" + commentId + "/delete").session(sessionOf(writer)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/comments/" + commentId + "/delete").session(sessionOf(other)))
                .andExpect(redirectedUrl("/board/" + postId));
        assertThat(commentRepository.existsById(commentId)).isFalse();
    }

    private Long writeComment(LoginMember author, String content) throws Exception {
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(author)).param("content", content));
        return commentRepository.findAll().get(0).getId();
    }
}
