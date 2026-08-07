package com.novamens.kbee.security;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;

import kbee.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KbeeLogFilter extends OncePerRequestFilter {

	private static Logger logger = Logger.getLogger(KbeeLogFilter.class.getName());
  
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

    	
    	
    	logger.debug ("Content-Type "+httpServletRequest.getHeader("Content-Type"));
    	logger.debug ("User-Agent "+httpServletRequest.getHeader("User-Agent"));
    	logger.debug ("Authorization "+httpServletRequest.getHeader("Authorization"));
    	
    	System.out.println(httpServletRequest.getHeader("Content-Type"));
    	System.out.println(httpServletRequest.getHeader("User-Agent"));
    	System.out.println(httpServletRequest.getHeader("Authorization"));
    	
    	
    	filterChain.doFilter(httpServletRequest,httpServletResponse);
    }

}
