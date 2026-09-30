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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KbeeOauth2Filter extends OncePerRequestFilter {

    private static final Logger logger =
            LogManager.getLogger(KbeeOauth2Filter.class);

    private Filter oauth2LoginAuthenticationFilter;
    private Filter oauth2LinkAccountFilter;
    private Filter oauth2TokenAuthenticationFilter;

    private final RedirectStrategy redirectStrategy =
            new DefaultRedirectStrategy();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug(
                "KbeeOauth2Filter - request {} {}",
                request.getMethod(),
                request.getRequestURI()
        );

        final boolean processed =
                forwardOauthRequest(
                        request,
                        response,
                        filterChain
                );

        logger.debug(
                "KbeeOauth2Filter - processed={}",
                processed
        );

        if (!processed) {
            filterChain.doFilter(
                    request,
                    response
            );
        }
    }

    private boolean forwardOauthRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws IOException, ServletException {

        MultiValueMap<String, String> params =
                KbeeOAuth2AuthorizationResponseUtils
                        .toMultiMap(
                                request.getParameterMap()
                        );

        /*
         * OAuth2 Resource Server:
         * request con Authorization: Bearer ...
         */
        String authHeader =
                request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer")) {

            logger.debug(
                    "KbeeOauth2Filter - Bearer token detected"
            );

            oauth2TokenAuthenticationFilter.doFilter(
                    request,
                    response,
                    filterChain
            );

            return true;
        }

        /*
         * Si no es una respuesta del Authorization Server,
         * dejamos continuar normalmente la cadena.
         */
        if (!KbeeOAuth2AuthorizationResponseUtils
                .isAuthorizationResponse(params)) {

            logger.debug(
                    "KbeeOauth2Filter - not an OAuth2 authorization response"
            );

            return false;
        }

        logger.info(
                "KbeeOauth2Filter - OAuth2 authorization response detected"
        );

        /*
         * NO logueamos code ni state completos.
         */
        logger.debug(
                "KbeeOauth2Filter - code present={}, state present={}, error present={}",
                params.getFirst("code") != null,
                params.getFirst("state") != null,
                params.getFirst("error") != null
        );

        final String state =
                params.getFirst("state");

        KbeeOauthStateKeyGen.Payload payload;

        try {

            payload =
                    KbeeOauthStateKeyGen
                            .decodeState(state);

        } catch (IOException e) {

            logger.error(
                    "KbeeOauth2Filter - error decoding OAuth2 state",
                    e
            );

            redirectStrategy.sendRedirect(
                    request,
                    response,
                    "error"
            );

            /*
             * Importante:
             * no continuar porque payload no existe.
             */
            return true;
        }

        if (payload == null) {

            logger.error(
                    "KbeeOauth2Filter - decoded state payload is null"
            );

            redirectStrategy.sendRedirect(
                    request,
                    response,
                    "error"
            );

            return true;
        }

        String operationStr =
                payload
                        .getAttributes()
                        .get("op");

        logger.info(
                "KbeeOauth2Filter - OAuth2 operation={}",
                operationStr
        );

        KbeeOauth2Operation operation =
                KbeeOauth2Operation
                        .fromName(operationStr);

        logger.debug(
                "KbeeOauth2Filter - resolved operation={}",
                operation
        );

        switch (operation) {

            case LOGIN:

                logger.info(
                        "KbeeOauth2Filter - forwarding to OAuth2LoginAuthenticationFilter"
                );

                oauth2LoginAuthenticationFilter.doFilter(
                        request,
                        response,
                        filterChain
                );

                logger.info(
                        "KbeeOauth2Filter - returned from OAuth2LoginAuthenticationFilter, status={}",
                        response.getStatus()
                );

                return true;

            case LINK_ACCOUNT:

                logger.info(
                        "KbeeOauth2Filter - forwarding to OAuth2LinkAccountFilter"
                );

                oauth2LinkAccountFilter.doFilter(
                        request,
                        response,
                        filterChain
                );

                logger.info(
                        "KbeeOauth2Filter - returned from OAuth2LinkAccountFilter, status={}",
                        response.getStatus()
                );

                return true;

            default:

                logger.info(
                        "KbeeOauth2Filter - default operation, forwarding to OAuth2LoginAuthenticationFilter"
                );

                oauth2LoginAuthenticationFilter.doFilter(
                        request,
                        response,
                        filterChain
                );

                logger.info(
                        "KbeeOauth2Filter - returned from OAuth2LoginAuthenticationFilter, status={}",
                        response.getStatus()
                );

                return true;
        }
    }

    public Filter getOauth2LoginAuthenticationFilter() {
        return oauth2LoginAuthenticationFilter;
    }

    public void setOauth2LoginAuthenticationFilter(
            Filter oauth2LoginAuthenticationFilter) {

        this.oauth2LoginAuthenticationFilter =
                oauth2LoginAuthenticationFilter;
    }

    public Filter getOauth2LinkAccountFilter() {
        return oauth2LinkAccountFilter;
    }

    public void setOauth2LinkAccountFilter(
            Filter oauth2LinkAccountFilter) {

        this.oauth2LinkAccountFilter =
                oauth2LinkAccountFilter;
    }

    public Filter getOauth2TokenAuthenticationFilter() {
        return oauth2TokenAuthenticationFilter;
    }

    public void setOauth2TokenAuthenticationFilter(
            Filter oauth2TokenAuthenticationFilter) {

        this.oauth2TokenAuthenticationFilter =
                oauth2TokenAuthenticationFilter;
    }
}