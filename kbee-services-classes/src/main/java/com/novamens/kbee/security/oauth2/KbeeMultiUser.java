package com.novamens.kbee.security.oauth2;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class KbeeMultiUser extends User implements OAuth2User {
	private static final long serialVersionUID = 1L;
	
	List<Long> userIds;

    public KbeeMultiUser(String username, String password, List<Long> userIds) {
        super(username, password, new ArrayList<>());
        this.userIds = userIds;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>();
    }

    @Override
    public String getName() {
        return getUsername();
    }

    public List<Long> getUserIds() {
        return userIds;
    }
}