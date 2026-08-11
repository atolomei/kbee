package com.novamens.content.user.externalLogin;

public enum UserExternalPlatformIdType {
    ID(1),
    EMAIL(2);
    private int id;

    UserExternalPlatformIdType(int id) {

        this.id = id;
    }

    public int getId() {
        return id;
    }
}
