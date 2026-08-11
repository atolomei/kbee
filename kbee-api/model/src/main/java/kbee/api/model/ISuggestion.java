package kbee.api.model;

import java.io.Serializable;

public class ISuggestion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String displayName;
    private String href;
    private String subline;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getSubline() {
        return subline;
    }

    public void setSubline(String subline) {
        this.subline = subline;
    }
}