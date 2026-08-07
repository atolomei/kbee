package com.novamens.kbee.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.novamens.kbee.security.oauth2.KbeeMultiUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DomainSelectRedirectFilter extends GenericFilterBean {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof KbeeMultiUser &&
                !((HttpServletRequest) request).getRequestURI().equals("/loginDomainSelection")) {
            ((HttpServletResponse) response).sendRedirect("/loginDomainSelection");
        } else {
            chain.doFilter(request, response);
        }
    }

}
