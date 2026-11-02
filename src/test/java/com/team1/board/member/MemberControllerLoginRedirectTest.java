package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.team1.board.common.LoginMember;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;

/**
 * A-04 특성화 테스트: 로그인 성공 후 복귀 주소(redirectURL) 검사·인코딩 결과를 public 경로 login() 으로 고정한다.
 * 검사 로직이 다른 클래스로 옮겨져도 이 테스트는 고치지 않는다.
 */
class MemberControllerLoginRedirectTest {

    private MemberController controller;

    @BeforeEach
    void setUp() {
        MemberService memberService = mock(MemberService.class);
        when(memberService.login(anyString(), anyString()))
                .thenReturn(Optional.of(new LoginMember(1000L, "minho", "m@t.kr", "김민호")));
        controller = new MemberController(memberService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"board", "https://evil.com", "//evil.com", "/\\evil.com", "/\t/evil.com",
            "/a\u0000", "/a\u001f", "/a\u007f", "/{x}", "/a}"})
    void unsafe_redirectsToRoot(String redirectURL) {
        assertThat(login(redirectURL)).isEqualTo("redirect:/");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "/|/",
            "/board/write|/board/write",
            "/board?keyword=%ED%95%9C|/board?keyword=%ED%95%9C",
            "/board?keyword=한|/board?keyword=%ED%95%9C",
            "/a+b|/a+b",
            "/~|/~",
            "'/a b'|/a%20b",
            "/\u0080|/%C2%80"
    })
    void internal_keptAndOnlyNonAsciiEncoded(String redirectURL, String expected) {
        assertThat(login(redirectURL)).isEqualTo("redirect:" + expected);
    }

    /** [현재 동작 고정·F-03] 서로게이트 쌍(이모지)은 글자 단위 인코딩 때문에 각 반쪽이 '?'(%3F)로 바뀐다. */
    @ParameterizedTest
    @ValueSource(strings = {"/😀"})
    void surrogatePair_currentlyBecomesQuestionMarks(String redirectURL) {
        assertThat(login(redirectURL)).isEqualTo("redirect:/%3F%3F");
    }

    private String login(String redirectURL) {
        MemberLoginForm form = new MemberLoginForm();
        form.setMemberEmail("m@t.kr");
        form.setMemberPassword("pass1234");
        return controller.login(form, new BeanPropertyBindingResult(form, "loginForm"), redirectURL,
                new MockHttpServletRequest(), new ExtendedModelMap());
    }
}
