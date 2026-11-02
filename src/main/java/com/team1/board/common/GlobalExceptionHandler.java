package com.team1.board.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttribute;

/** 404·403 안내 화면(REQ-NF-002)과 모든 화면 공통의 loginMember 모델 속성. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ModelAttribute("loginMember")
    public LoginMember loginMember(
            @SessionAttribute(name = SessionConst.LOGIN_INFO, required = false) LoginMember loginMember) {
        return loginMember;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound() {
        return "error/404";
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden() {
        return "error/403";
    }
}
