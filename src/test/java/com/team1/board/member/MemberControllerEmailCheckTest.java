package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** A-03 특성화 테스트: 이메일 중복 확인의 20자 상한·trim·형식 검사를 고정한다. */
class MemberControllerEmailCheckTest {

    private MemberService memberService;
    private MemberController controller;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        controller = new MemberController(memberService);
    }

    @Test
    void twentyCharacters_askService() {
        String email = MemberJoinFormLengthTest.email(20);
        when(memberService.isEmailAvailable(email)).thenReturn(true);
        assertThat(controller.emailCheck(email)).isEqualTo("OK");
        when(memberService.isEmailAvailable(email)).thenReturn(false);
        assertThat(controller.emailCheck(email)).isEqualTo("NO");
    }

    @Test
    void over20_noWithoutAskingService() {
        assertThat(controller.emailCheck(MemberJoinFormLengthTest.email(21))).isEqualTo("NO");
        verify(memberService, never()).isEmailAvailable(anyString());
    }

    @Test
    void trimmedBeforeLengthCheck() {
        String email = MemberJoinFormLengthTest.email(20);
        when(memberService.isEmailAvailable(email)).thenReturn(true);
        assertThat(controller.emailCheck("  " + email + "  ")).isEqualTo("OK");
    }

    @Test
    void badFormatOrBlank_no() {
        assertThat(controller.emailCheck("not-an-email")).isEqualTo("NO");
        assertThat(controller.emailCheck("")).isEqualTo("NO");
        verify(memberService, never()).isEmailAvailable(anyString());
    }
}
