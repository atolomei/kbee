package com.novamens.content.user.externalLogin;

public enum ExternalPlatformId {
    GOOGLE(1, "Google"),
    FACEBOOK(2, "Facebook"),
    AZURE(3, "Microsoft Azure");

    private int id;
    private String description;

    ExternalPlatformId(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public static ExternalPlatformId fromName(String name) {
        switch (name.toLowerCase()) {
            case "google":
                return GOOGLE;
            case "facebook":
                return FACEBOOK;
            case "microsoft azure":
                return AZURE;
            default:
                return null;
        }
    }

    public static ExternalPlatformId fromId(int id) {
        if (id == GOOGLE.getId())
            return GOOGLE;
        if (id == FACEBOOK.getId())
            return FACEBOOK;

        return null;
    }
}
