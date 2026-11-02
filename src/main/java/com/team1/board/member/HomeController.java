package com.team1.board.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 메인 화면(REQ-F-009). 로그인 상태는 공통 모델 속성 loginMember 로 판단한다. */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
