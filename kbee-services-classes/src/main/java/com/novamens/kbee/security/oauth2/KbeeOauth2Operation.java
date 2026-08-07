package com.novamens.kbee.security.oauth2;

public enum KbeeOauth2Operation {
    LOGIN("login"),
    LINK_ACCOUNT("link_account");


    private String name;

    KbeeOauth2Operation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static KbeeOauth2Operation fromName(String name){
        if(LOGIN.getName().equals(name))
            return LOGIN;
        else if (LINK_ACCOUNT.getName().equals(name))
            return LINK_ACCOUNT;

        return null;
    }
}
