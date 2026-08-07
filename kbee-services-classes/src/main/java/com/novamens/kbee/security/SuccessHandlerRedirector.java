package com.novamens.kbee.security;

import com.novamens.beans.BeansService;
import com.novamens.service.ServiceLocator;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SuccessHandlerRedirector implements LogoutSuccessHandler {

	   @Override
	   public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		   LogoutSuccessHandler successHandler = (LogoutSuccessHandler) ServiceLocator.getService(BeansService.class).getBean("logoutSuccessHandler");
			successHandler.onLogoutSuccess(request,response,authentication);

	   }
}
