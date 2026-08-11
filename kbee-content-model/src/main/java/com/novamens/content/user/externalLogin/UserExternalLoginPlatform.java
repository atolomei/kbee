package com.novamens.content.user.externalLogin;

import com.novamens.content.user.UserProfile;

public interface UserExternalLoginPlatform {
    void setId(Long id);

    Long getId();

    UserProfile getUserProfile();

    void setUserProfile(UserProfile userProfile);

    void setPlatformId(int platformId);

    int getPlatformId();

    void setUserPlatformIdType(int userPlatformIdType);

    int getUserPlatformIdType();

    void setUserPlatformId(String userPlatformId);

    String getUserPlatformId();

    void setEnabled(Boolean enabled);

    Boolean getEnabled();
}
