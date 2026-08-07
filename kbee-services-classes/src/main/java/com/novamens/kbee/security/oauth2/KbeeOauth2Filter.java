package com.novamens.kbee.security.oauth2;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KbeeOauth2Filter extends OncePerRequestFilter {

    Filter oauth2LoginAuthenticationFilter;
    Filter oauth2LinkAccountFilter;
    Filter oauth2TokenAuthenticationFilter;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final boolean processed = forwardOauthRequest(httpServletRequest, httpServletResponse, filterChain);

        if(!processed)
            filterChain.doFilter(httpServletRequest,httpServletResponse);
    }

    private boolean forwardOauthRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        
    	MultiValueMap<String, String> params = KbeeOAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
        
    	
    	String authheader  = request.getHeader("Authorization");
    	if (authheader!=null && authheader.startsWith("Bearer")) {
    		oauth2TokenAuthenticationFilter.doFilter(request,response,filterChain);
            return true;
    	}
        
    	
    	if (!KbeeOAuth2AuthorizationResponseUtils.isAuthorizationResponse(params)) {
            return false;
        }
    	

        final String state = params.getFirst("state");
        KbeeOauthStateKeyGen.Payload payload = null;
        try {
            payload = KbeeOauthStateKeyGen.decodeState(state);
        } 
        catch (IOException e) {
            redirectStrategy.sendRedirect(request,response, "error");
        }

        String operationStr = payload.getAttributes().get("op");
        KbeeOauth2Operation operation = KbeeOauth2Operation.fromName( operationStr);

        switch (operation) {
            case LOGIN:
                oauth2LoginAuthenticationFilter.doFilter(request,response,filterChain);
                return true;
            case LINK_ACCOUNT:
                oauth2LinkAccountFilter.doFilter(request,response,filterChain);
                return true;
            default:
                oauth2LoginAuthenticationFilter.doFilter(request,response,filterChain);
                return true;
        }
    }

    public Filter getOauth2LoginAuthenticationFilter() {
        return oauth2LoginAuthenticationFilter;
    }

    public void setOauth2LoginAuthenticationFilter(Filter oauth2LoginAuthenticationFilter) {
        this.oauth2LoginAuthenticationFilter = oauth2LoginAuthenticationFilter;
    }

    public Filter getOauth2LinkAccountFilter() {
        return oauth2LinkAccountFilter;
    }

    public void setOauth2LinkAccountFilter(Filter oauth2LinkAccountFilter) {
        this.oauth2LinkAccountFilter = oauth2LinkAccountFilter;
    }

	public Filter getOauth2TokenAuthenticationFilter() {
		return oauth2TokenAuthenticationFilter;
	}
	            
	public void setOauth2TokenAuthenticationFilter(Filter oauth2TokenAuthenticationFilter) {
		this.oauth2TokenAuthenticationFilter = oauth2TokenAuthenticationFilter;
	}
}
