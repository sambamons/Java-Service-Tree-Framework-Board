package com.team1.board.common;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** 세션 기반 인증 필터(REQ-SE-001). 인증 방식은 이 필터 하나뿐이다(REQ-SE-005). */
public class LoginFilter implements Filter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/", "/member/join", "/member/login", "/member/logout", "/member/email-check", "/error");
    private static final String PUBLIC_PREFIX = "/css/";
    private static final Pattern PUBLIC_GET_BOARD = Pattern.compile("^/board(/\\d+)?$");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        if (isPublic(path, httpRequest.getMethod()) || isLoggedIn(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String loginUrl = httpRequest.getContextPath() + "/member/login";
        // POST 등의 URI 로 GET 복귀하면 405 가 나므로 GET 요청에만 복귀 경로를 붙인다
        if ("GET".equals(httpRequest.getMethod())) {
            String target = httpRequest.getQueryString() == null
                    ? path : path + "?" + httpRequest.getQueryString();
            loginUrl += "?redirectURL=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
        }
        ((HttpServletResponse) response).sendRedirect(loginUrl);
    }

    private boolean isPublic(String path, String method) {
        // 정규화되지 않은 경로는 라우팅과 해석이 어긋날 수 있으므로 공개로 보지 않는다
        if (path.contains("..") || path.contains(";") || path.contains("//")) {
            return false;
        }
        if (PUBLIC_PATHS.contains(path) || path.startsWith(PUBLIC_PREFIX)) {
            return true;
        }
        return "GET".equals(method) && PUBLIC_GET_BOARD.matcher(path).matches();
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SessionConst.LOGIN_INFO) != null;
    }
}
