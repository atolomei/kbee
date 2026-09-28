package com.novamens.kbee.security.oauth2;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class KbeeOidcUser extends User implements OidcUser {

    private static final long serialVersionUID = 1L;

    private final OidcUser oidcUser;

    public KbeeOidcUser(
            com.novamens.security.User user,
            OidcUser oidcUser,
            Collection<? extends GrantedAuthority> authorities) {

        super(
            user.getName(),
            getPassword(user),
            user.isEnabled(),
            true,
            true,
            true,
            authorities
        );

        this.oidcUser = oidcUser;
    }

    private static String getPassword(
    		com.novamens.security.User user) {

        if (user.getPassword() != null) {
            return user.getPassword();
        }

        return UUID.randomUUID().toString();
    }

    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }
}