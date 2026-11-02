package com.team1.board.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.team1.board.IntegrationTestSupport;
import com.team1.board.board.CommentForm;
import com.team1.board.board.CommentService;
import com.team1.board.common.LoginMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceTest extends IntegrationTestSupport {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CommentService commentService;

    /** REQ-SE-002: 저장된 비밀번호는 원문이 아닌 BCrypt 해시다. */
    @Test
    void signUp_storesBCryptHash() {
        createMember("minho", "m@test.com", "김민호");
        String stored = memberRepository.findByMemberEmail("m@test.com").orElseThrow().getMemberPassword();
        assertThat(stored).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(passwordEncoder.matches(PASSWORD, stored)).isTrue();
    }

    /** REQ-F-008: 탈퇴 시 내 글·내 댓글·내 글의 남 댓글까지 FK 오류 없이 지운다. */
    @Test
    void delete_cascadesPostsAndComments() {
        LoginMember me = createMember("minho", "m@test.com", "김민호");
        LoginMember other = createMember("inhu", "i@test.com", "김인후");
        Long myPost = createPost(me, "내 글", "내용");
        Long otherPost = createPost(other, "남의 글", "내용");
        commentService.write(myPost, comment("남이 내 글에"), other);
        commentService.write(otherPost, comment("내가 남의 글에"), me);
        commentService.write(otherPost, comment("남이 남의 글에"), other);

        memberService.delete(me.getId(), me);

        assertThat(memberRepository.existsById(me.getId())).isFalse();
        assertThat(boardRepository.existsById(myPost)).isFalse();
        assertThat(boardRepository.existsById(otherPost)).isTrue();
        assertThat(commentRepository.count()).isEqualTo(1);
    }

    private CommentForm comment(String content) {
        CommentForm form = new CommentForm();
        form.setContent(content);
        return form;
    }
}
