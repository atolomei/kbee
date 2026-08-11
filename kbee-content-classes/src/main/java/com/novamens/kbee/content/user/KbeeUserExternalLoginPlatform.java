package com.novamens.kbee.content.user;

import com.novamens.content.user.UserProfile;
import com.novamens.content.user.externalLogin.UserExternalLoginPlatform;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@org.hibernate.annotations.Cache(usage= CacheConcurrencyStrategy.READ_WRITE, region="entity")
@Table(name = "user_external_login_platform")
public class KbeeUserExternalLoginPlatform  implements UserExternalLoginPlatform {

    @Id
    @SequenceGenerator(name = "label_sequencer", sequenceName = "entityid_sequence")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "label_sequencer")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity= KbeeUserProfile.class)
    @JoinColumn(name = "USERPROFILE_ID", nullable=false)
    private UserProfile userProfile;

    @Column(name = "platform_id")
    private int platformId;

    @Column(name = "user_platform_id_type")
    private int userPlatformIdType;

    @Column(name = "user_platform_id")
    private String userPlatformId;

    @Column(name = "enabled")
    private Boolean enabled;

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public UserProfile getUserProfile() {
        return userProfile;
    }

    @Override
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @Override
    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    @Override
    public int getPlatformId() {
        return platformId;
    }

    @Override
    public void setUserPlatformIdType(int userPlatformIdType) {
        this.userPlatformIdType = userPlatformIdType;
    }

    @Override
    public int getUserPlatformIdType() {
        return userPlatformIdType;
    }

    @Override
    public void setUserPlatformId(String userPlatformId) {
        this.userPlatformId = userPlatformId;
    }

    @Override
    public String getUserPlatformId() {
        return userPlatformId;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Boolean getEnabled() {
        return enabled;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KbeeUserExternalLoginPlatform that = (KbeeUserExternalLoginPlatform) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
