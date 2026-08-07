
package com.novamens.kbee.security.oauth2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.Assert;

public final class KbeeJwtAuthenticationProvider implements AuthenticationProvider  {

	private final Log logger = LogFactory.getLog(getClass());

	private final JwtDecoder jwtDecoder;

	private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtAuthenticationConverter();

	public KbeeJwtAuthenticationProvider(JwtDecoder jwtDecoder) {
		Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
		this.jwtDecoder = jwtDecoder;
	}

	/**
	 * Decode and validate the
	 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer
	 * Token</a>.
	 * @param authentication the authentication request object.
	 * @return A successful authentication
	 * @throws AuthenticationException if authentication failed for some reason
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
		Jwt jwt = getJwt(bearer);
		AbstractAuthenticationToken token = ((KbeeJwtAuthenticationConverter)this.jwtAuthenticationConverter).convert(jwt, authentication);
		token.setDetails(bearer.getDetails());
		this.logger.debug("Authenticated token");
		return token;
	}

	private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
		try {
			return this.jwtDecoder.decode(bearer.getToken());
		}
		catch (BadJwtException failed) {
			this.logger.debug("Failed to authenticate since the JWT was invalid");
			throw new InvalidBearerTokenException(failed.getMessage(), failed);
		}
		catch (JwtException failed) {
			throw new AuthenticationServiceException(failed.getMessage(), failed);
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public void setJwtAuthenticationConverter(
			Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) {
		Assert.notNull(jwtAuthenticationConverter, "jwtAuthenticationConverter cannot be null");
		this.jwtAuthenticationConverter = jwtAuthenticationConverter;
	}

}
