package com.novamens.kbee.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	   // Just for setting the default target URL
	   public DefaultSuccessHandler(String defaultTargetURL) {
	        this.setDefaultTargetUrl(defaultTargetURL);
	   }

	   @Override
	   public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
	       super.onLogoutSuccess(request, response, authentication);
	   }
}
