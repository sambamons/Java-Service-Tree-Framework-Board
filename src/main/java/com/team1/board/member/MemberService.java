package com.team1.board.member;

import com.team1.board.board.BoardRepository;
import com.team1.board.board.CommentRepository;
import com.team1.board.common.ForbiddenException;
import com.team1.board.common.LoginMember;
import com.team1.board.common.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, BoardRepository boardRepository,
                         CommentRepository commentRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 회원가입(REQ-F-001). 비밀번호는 BCrypt 해시로 저장한다(REQ-SE-002). */
    @Transactional
    public Long signUp(MemberJoinForm form) {
        if (memberRepository.existsByMemberId(form.getMemberId())) {
            throw new DuplicateMemberException("memberId", "이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByMemberEmail(form.getMemberEmail())) {
            throw new DuplicateMemberException("memberEmail", "이미 사용 중인 이메일입니다.");
        }
        MemberEntity member = new MemberEntity(form.getMemberId(), form.getMemberEmail(),
                passwordEncoder.encode(form.getMemberPassword()), form.getMemberName());
        try {
            return memberRepository.saveAndFlush(member).getId();
        } catch (DataIntegrityViolationException e) {
            // 동시 가입 경합으로 유일 제약에 걸린 경우
            throw new DuplicateMemberException("memberEmail", "이미 사용 중인 아이디 또는 이메일입니다.");
        }
    }

    /** 이메일 중복 확인(REQ-F-002). */
    public boolean isEmailAvailable(String memberEmail) {
        return !memberRepository.existsByMemberEmail(memberEmail);
    }

    /** 이메일·비밀번호 로그인(REQ-F-003). 계정 없음과 비밀번호 불일치를 구분하지 않는다. */
    public Optional<LoginMember> login(String memberEmail, String rawPassword) {
        return memberRepository.findByMemberEmail(memberEmail)
                .filter(member -> passwordEncoder.matches(rawPassword, member.getMemberPassword()))
                .map(member -> new LoginMember(member.getId(), member.getMemberId(),
                        member.getMemberEmail(), member.getMemberName()));
    }

    public List<MemberDTO> findAll() {
        return memberRepository.findAll().stream().map(MemberDTO::from).collect(Collectors.toList());
    }

    public MemberDTO findById(Long id) {
        return memberRepository.findById(id).map(MemberDTO::from)
                .orElseThrow(() -> new NotFoundException("회원이 없습니다: " + id));
    }

    /** 회원 삭제(REQ-F-008). 관리자 역할이 없으므로 본인만 삭제할 수 있다(REQ-SE-004). */
    @Transactional
    public void delete(Long id, LoginMember actor) {
        if (!memberRepository.existsById(id)) {
            throw new NotFoundException("회원이 없습니다: " + id);
        }
        if (!actor.isSelf(id)) {
            throw new ForbiddenException("본인만 삭제할 수 있습니다.");
        }
        commentRepository.deleteByWriterId(id);
        commentRepository.deleteByBoardWriterId(id);
        boardRepository.deleteByWriterId(id);
        memberRepository.deleteById(id);
    }
}
