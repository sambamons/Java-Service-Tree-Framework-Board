package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.common.LoginMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** A-02 특성화 테스트: 댓글 작성·수정·삭제 뒤 돌아가는 주소와 검증 오류 flash 를 고정한다. */
class CommentControllerRedirectTest extends IntegrationTestSupport {

    LoginMember writer;
    LoginMember other;
    Long postId;
    Long commentId;

    @BeforeEach
    void setUp() throws Exception {
        writer = createMember("inhu", "i@test.com", "김인후");
        other = createMember("minho", "m@test.com", "김민호");
        postId = createPost(writer, "제목", "내용");
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(writer)).param("content", "원래 댓글"));
        commentId = commentRepository.findAll().get(0).getId();
    }

    @Test
    void edit_blankByAuthor_redirectsToPostWithError() throws Exception {
        mockMvc.perform(post("/comments/" + commentId + "/edit").session(sessionOf(writer)).param("content", " "))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/board/" + postId))
                .andExpect(flash().attribute("commentError", "댓글을 입력하세요."));
        assertThat(commentRepository.findById(commentId).orElseThrow().getContent()).isEqualTo("원래 댓글");
    }

    @Test
    void edit_blankByOther_403WithoutFlash() throws Exception {
        mockMvc.perform(post("/comments/" + commentId + "/edit").session(sessionOf(other)).param("content", " "))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(flash().attributeCount(0));
    }

    @Test
    void edit_blankOnMissingComment_404() throws Exception {
        mockMvc.perform(post("/comments/999999/edit").session(sessionOf(writer)).param("content", " "))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void write_blank_redirectsWithMessage() throws Exception {
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(other)).param("content", " "))
                .andExpect(redirectedUrl("/board/" + postId))
                .andExpect(flash().attribute("commentError", "댓글을 입력하세요."));
    }

    @Test
    void write_tooLong_redirectsWithMessage() throws Exception {
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(other)).param("content", "가".repeat(501)))
                .andExpect(redirectedUrl("/board/" + postId))
                .andExpect(flash().attribute("commentError", "댓글은 500자 이내여야 합니다."));
    }

    @Test
    void writeEditDelete_redirectToOwningPost() throws Exception {
        Long otherPostId = createPost(writer, "다른 글", "내용");
        mockMvc.perform(post("/board/" + otherPostId + "/comments").session(sessionOf(other)).param("content", "새 댓글"))
                .andExpect(redirectedUrl("/board/" + otherPostId));
        mockMvc.perform(post("/comments/" + commentId + "/edit").session(sessionOf(writer)).param("content", "고친 댓글"))
                .andExpect(redirectedUrl("/board/" + postId));
        mockMvc.perform(post("/comments/" + commentId + "/delete").session(sessionOf(writer)))
                .andExpect(redirectedUrl("/board/" + postId));
    }
}
