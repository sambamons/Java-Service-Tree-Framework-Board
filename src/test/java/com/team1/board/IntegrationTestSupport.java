package com.team1.board;

import com.team1.board.board.BoardForm;
import com.team1.board.board.BoardRepository;
import com.team1.board.board.BoardService;
import com.team1.board.board.CommentRepository;
import com.team1.board.common.LoginMember;
import com.team1.board.common.SessionConst;
import com.team1.board.member.MemberJoinForm;
import com.team1.board.member.MemberRepository;
import com.team1.board.member.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc 통합 테스트 공통. 운영과 같이 뷰 렌더링이 트랜잭션 밖에서 일어나도록
 * {@code @Transactional} 을 쓰지 않고, 매 테스트 전에 FK 역순으로 데이터를 지운다.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    protected static final String PASSWORD = "pass1234";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected MemberService memberService;
    @Autowired
    protected BoardService boardService;
    @Autowired
    protected MemberRepository memberRepository;
    @Autowired
    protected BoardRepository boardRepository;
    @Autowired
    protected CommentRepository commentRepository;

    @BeforeEach
    void cleanDatabase() {
        commentRepository.deleteAllInBatch();
        boardRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    protected LoginMember createMember(String memberId, String email, String name) {
        MemberJoinForm form = new MemberJoinForm();
        form.setMemberId(memberId);
        form.setMemberEmail(email);
        form.setMemberPassword(PASSWORD);
        form.setMemberName(name);
        memberService.signUp(form);
        return memberService.login(email, PASSWORD).orElseThrow();
    }

    protected MockHttpSession sessionOf(LoginMember loginMember) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionConst.LOGIN_INFO, loginMember);
        return session;
    }

    protected Long createPost(LoginMember writer, String title, String content) {
        return boardService.write(new BoardForm(title, content), writer);
    }
}
