package com.sai.slog.app.filter;


import com.sai.slog.app.model.LoginBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Login Filter intercepts every request.
 * 1. Checks if the given url is to be avoided.
 * 1. Checks if the user is logged in.
 * 2. If the user is logged in , then checks if the user is authorised to check the following page resource.
 * 3. If the user is authorised , then the request is passed to the next filter.
 * 4. If the user is not logged in, it redirects the current page to landingPage.xhtml.
 *
 * @author Kumar Thangavel
 */
public class AuthFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        // Get the loginBean from session attribute
        LoginBean loginBean = (LoginBean) httpServletRequest.getSession().getAttribute("user");

        if (!isUrlToAvoid(httpServletRequest)) {
            if (((HttpServletRequest) request).getRequestURI().contains("logout.do")) {
                httpServletRequest.getSession(false).invalidate();
                loginBean = null;
            }
            if (loginBean == null) {
                String contextPath = httpServletRequest.getContextPath();
                httpServletResponse.sendRedirect(contextPath + "/public.do");
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isUrlToAvoid(final HttpServletRequest request) {
        return (request.getRequestURI().contains("/public.do"))
                || (request.getRequestURI().contains("/javax.faces.resource")
                || (request.getRequestURI().contains(".png")));
    }

    @Override
    public void destroy() {

    }
}