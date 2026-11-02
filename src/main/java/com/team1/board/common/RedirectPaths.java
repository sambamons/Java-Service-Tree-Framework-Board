package com.team1.board.common;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** 로그인 뒤 돌아갈 주소(redirectURL) 검사. LoginFilter 가 붙인 복귀 주소와 짝을 이룬다. */
public final class RedirectPaths {

    private RedirectPaths() {
    }

    /**
     * 내부 경로만 허용한다(open redirect 방지). 브라우저가 지우는 제어문자, 역슬래시,
     * redirect: 의 URI 템플릿으로 해석되는 중괄호가 있으면 거부한다. 필터가 넘긴 값은 쿼리가 이미
     * 인코딩돼 있으므로 '%' 는 그대로 두고 공백·비ASCII 문자만 인코딩한다.
     */
    public static String safeInternal(String redirectURL) {
        if (redirectURL == null || !redirectURL.startsWith("/") || redirectURL.startsWith("//")
                || redirectURL.chars().anyMatch(c -> c < 0x20 || c == 0x7f || c == '\\' || c == '{' || c == '}')) {
            return "/";
        }
        StringBuilder encoded = new StringBuilder();
        for (char c : redirectURL.toCharArray()) {
            if (c == ' ' || c > 0x7e) {
                encoded.append(URLEncoder.encode(String.valueOf(c), StandardCharsets.UTF_8).replace("+", "%20"));
            } else {
                encoded.append(c);
            }
        }
        return encoded.toString();
    }
}
