package com.team1.board.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.common.LoginMember;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class BoardControllerTest extends IntegrationTestSupport {

    @Test
    void list_public_pagedBy10() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        for (int i = 1; i <= 15; i++) {
            createPost(writer, "글" + i, "내용");
        }
        mockMvc.perform(get("/board"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", hasSize(10)))
                .andExpect(model().attribute("totalPages", 2));
        mockMvc.perform(get("/board").param("page", "2"))
                .andExpect(model().attribute("posts", hasSize(5)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void list_sortLatestAndOldest() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        Long first = createPost(writer, "처음", "내용");
        Long last = createPost(writer, "나중", "내용");
        List<BoardSummary> latest = (List<BoardSummary>) mockMvc.perform(get("/board"))
                .andReturn().getModelAndView().getModel().get("posts");
        List<BoardSummary> oldest = (List<BoardSummary>) mockMvc.perform(get("/board").param("sort", "oldest"))
                .andReturn().getModelAndView().getModel().get("posts");
        assertThat(latest.get(0).getId()).isEqualTo(last);
        assertThat(oldest.get(0).getId()).isEqualTo(first);
    }

    @Test
    void list_searchByTitleContentWriter() throws Exception {
        LoginMember inhu = createMember("inhu", "i@test.com", "김인후");
        LoginMember minho = createMember("minho", "m@test.com", "김민호");
        createPost(inhu, "스프링 질문", "부트 설정");
        createPost(minho, "자바 질문", "스프링 부트 버전");
        mockMvc.perform(get("/board").param("type", "title").param("keyword", "스프링"))
                .andExpect(model().attribute("posts", hasSize(1)));
        mockMvc.perform(get("/board").param("type", "content").param("keyword", "스프링"))
                .andExpect(model().attribute("posts", hasSize(1)));
        mockMvc.perform(get("/board").param("type", "writer").param("keyword", "민호"))
                .andExpect(model().attribute("posts", hasSize(1)));
        mockMvc.perform(get("/board").param("type", "title").param("keyword", "질문"))
                .andExpect(model().attribute("posts", hasSize(2)));
    }

    @Test
    void detail_showsPostWithEscapedContent() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        Long postId = createPost(writer, "제목", "<script>alert(1)</script>");
        mockMvc.perform(get("/board/" + postId))
                .andExpect(status().isOk()).andExpect(view().name("board/detail"))
                .andExpect(content().string(containsString("김인후")))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)"))));
    }

    @Test
    void detail_missing_returns404() throws Exception {
        mockMvc.perform(get("/board/999999")).andExpect(status().isNotFound()).andExpect(view().name("error/404"));
    }

    @Test
    void write_withoutLogin_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/board/write").param("title", "t").param("content", "c"))
                .andExpect(status().isFound()).andExpect(redirectedUrlPattern("/member/login*"));
        assertThat(boardRepository.count()).isZero();
    }

    @Test
    void write_success_showsInList() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        mockMvc.perform(post("/board/write").session(sessionOf(writer)).param("title", "첫 글").param("content", "본문"))
                .andExpect(redirectedUrlPattern("/board/*"));
        mockMvc.perform(get("/board")).andExpect(content().string(containsString("첫 글")));
    }

    @Test
    void write_blankTitle_showsFormError() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        mockMvc.perform(post("/board/write").session(sessionOf(writer)).param("title", " ").param("content", "본문"))
                .andExpect(status().isOk()).andExpect(view().name("board/form"))
                .andExpect(model().attributeHasFieldErrors("boardForm", "title"));
    }

    @Test
    void edit_byAuthor_updates_byOther_returns403() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        LoginMember other = createMember("minho", "m@test.com", "김민호");
        Long postId = createPost(writer, "원래", "내용");

        mockMvc.perform(get("/board/" + postId + "/edit").session(sessionOf(other))).andExpect(status().isForbidden());
        mockMvc.perform(post("/board/" + postId + "/edit").session(sessionOf(other))
                        .param("title", "탈취").param("content", "x"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/board/" + postId + "/edit").session(sessionOf(writer))
                        .param("title", "수정됨").param("content", "새 내용"))
                .andExpect(redirectedUrlPattern("/board/*"));
        assertThat(boardRepository.findById(postId).orElseThrow().getTitle()).isEqualTo("수정됨");
    }

    @Test
    void edit_validationFailure_keepsEditMode() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        Long postId = createPost(writer, "원래", "내용");
        mockMvc.perform(post("/board/" + postId + "/edit").session(sessionOf(writer))
                        .param("title", "").param("content", "내용"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("mode", "edit"))
                .andExpect(model().attribute("postId", postId));
    }

    @Test
    void delete_byAuthor_removesPostWithComments() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        LoginMember other = createMember("minho", "m@test.com", "김민호");
        Long postId = createPost(writer, "제목", "내용");
        mockMvc.perform(post("/board/" + postId + "/comments").session(sessionOf(other)).param("content", "댓글"));

        mockMvc.perform(post("/board/" + postId + "/delete").session(sessionOf(other))).andExpect(status().isForbidden());
        mockMvc.perform(post("/board/" + postId + "/delete").session(sessionOf(writer)))
                .andExpect(redirectedUrl("/board"));
        assertThat(boardRepository.existsById(postId)).isFalse();
        assertThat(commentRepository.count()).isZero();
    }

    /** MySQL TEXT(64KB) 초과 500 을 막기 위해 본문 길이를 검증한다. */
    @Test
    void write_contentTooLong_showsFormError() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        mockMvc.perform(post("/board/write").session(sessionOf(writer))
                        .param("title", "제목").param("content", "가".repeat(10001)))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("boardForm", "content"));
        assertThat(boardRepository.count()).isZero();
    }

    /** 125건(13쪽): 번호는 현재 묶음만, « ‹ › » 는 각자 정해진 페이지를 가리킨다. */
    @Test
    void pagination_showsOnlyCurrentBlockWithMoveLinks() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        for (int i = 1; i <= 125; i++) {
            createPost(writer, "글" + i, "내용");
        }

        MvcResult lastResult = mockMvc.perform(get("/board").param("page", "13")).andReturn();
        PageBlock block = (PageBlock) lastResult.getModelAndView().getModel().get("pageBlock");
        assertThat(block.getStart()).isEqualTo(11);
        assertThat(block.getEnd()).isEqualTo(13);
        String last = pagesOf(lastResult);
        assertThat(numbersIn(last)).containsExactly(11, 12, 13);
        assertThat(currentIn(last)).isEqualTo("13");
        assertThat(moveHref(last, "처음 페이지")).isEqualTo("1");
        assertThat(moveHref(last, "이전 페이지")).isEqualTo("12");
        assertThat(isDisabled(last, "다음 페이지")).isTrue();
        assertThat(isDisabled(last, "마지막 페이지")).isTrue();

        String first = pagesOf(mockMvc.perform(get("/board").param("page", "1")).andReturn());
        assertThat(numbersIn(first)).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat(currentIn(first)).isEqualTo("1");
        assertThat(isDisabled(first, "처음 페이지")).isTrue();
        assertThat(isDisabled(first, "이전 페이지")).isTrue();
        assertThat(moveHref(first, "다음 페이지")).isEqualTo("2");
        assertThat(moveHref(first, "마지막 페이지")).isEqualTo("13");

        MvcResult outResult = mockMvc.perform(get("/board").param("page", "99"))
                .andExpect(status().isOk()).andReturn();
        String outOfRange = pagesOf(outResult);
        assertThat(numbersIn(outOfRange)).containsExactly(11, 12, 13);
        assertThat(currentIn(outOfRange)).isNull();
        assertThat(moveHref(outOfRange, "이전 페이지")).isEqualTo("13");
        assertThat(isDisabled(outOfRange, "다음 페이지")).isTrue();
    }

    /** 3쪽 중 2쪽: 번호·처음·이전·다음·마지막 링크 모두 검색·정렬 조건을 유지하고, 특수문자 검색어도 인코딩된다. */
    @Test
    void pagination_keepsSearchConditionsInAllLinks() throws Exception {
        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        String keyword = "a&b\"<x>";
        for (int i = 1; i <= 25; i++) {
            createPost(writer, keyword + i, "내용");
        }
        String pages = pagesOf(mockMvc.perform(get("/board").param("page", "2")
                .param("sort", "oldest").param("type", "title").param("keyword", keyword)).andReturn());

        Matcher href = Pattern.compile("<a href=\"([^\"]*)\"").matcher(pages);
        int links = 0;
        while (href.find()) {
            links++;
            assertThat(href.group(1)).contains("sort=oldest").contains("type=title")
                    .contains("keyword=a%26b%22%3Cx%3E");
        }
        assertThat(links).as("« ‹ 1 2 3 › » 모두 활성").isEqualTo(7);
        assertThat(pages).doesNotContain("<x>");
        assertThat(pages.split("aria-current", -1)).as("aria-current 는 현재 페이지에만").hasSize(2);
    }

    @Test
    void pagination_shownOnlyWhenMoreThanOnePage() throws Exception {
        mockMvc.perform(get("/board")).andExpect(content().string(not(containsString("aria-label=\"페이지 이동\""))));

        LoginMember writer = createMember("inhu", "i@test.com", "김인후");
        for (int i = 1; i <= 11; i++) {
            createPost(writer, "글" + i, "내용");
        }
        mockMvc.perform(get("/board")).andExpect(content().string(containsString("aria-label=\"페이지 이동\"")));
        // 검색 결과가 1쪽이면 숨긴다
        mockMvc.perform(get("/board").param("keyword", "글1"))
                .andExpect(content().string(not(containsString("aria-label=\"페이지 이동\""))));
        mockMvc.perform(get("/board").param("page", "0")).andExpect(model().attribute("page", 1));
    }

    /** 페이지 이동 영역(nav)만 잘라 단언 범위를 좁힌다. */
    private String pagesOf(MvcResult result) throws Exception {
        String html = result.getResponse().getContentAsString();
        int from = html.indexOf("aria-label=\"페이지 이동\"");
        assertThat(from).as("페이지 이동 영역").isPositive();
        return html.substring(from, html.indexOf("</nav>", from));
    }

    private List<Integer> numbersIn(String pages) {
        List<Integer> numbers = new ArrayList<>();
        Matcher m = Pattern.compile(">(\\d+)</a>").matcher(pages);
        while (m.find()) {
            numbers.add(Integer.parseInt(m.group(1)));
        }
        return numbers;
    }

    private String currentIn(String pages) {
        Matcher m = Pattern.compile("aria-current=\"page\"[^>]*>(\\d+)</a>").matcher(pages);
        return m.find() ? m.group(1) : null;
    }

    private String moveHref(String pages, String label) {
        Matcher m = Pattern.compile("<a href=\"/board\\?page=(\\d+)[^\"]*\"[^>]*aria-label=\"" + label + "\"").matcher(pages);
        return m.find() ? m.group(1) : null;
    }

    private boolean isDisabled(String pages, String label) {
        return pages.contains("aria-disabled=\"true\" aria-label=\"" + label + "\"");
    }
}
