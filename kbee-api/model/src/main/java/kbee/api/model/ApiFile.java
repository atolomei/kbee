package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ApiFile extends ApiClassificable {
    private static final long serialVersionUID = 1L;

    private int version;
    private String oId;
    private String title;
    private String subtitle;
    private String application;
    private String externalId;
    private String className;

    private ApiProxy contentClass;
    private List<ApiProxy> relationships;
    private ApiProxy previousVersion;
    private ApiProxy workspace;

    private List<ApiResource> resources = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    private List<ICustomAttributeValue> customAttributes;

    @Setter(AccessLevel.NONE)
    private List<ICustomAttributeValue> controlAttributes;

    private List<ApiFormData> forms;

    public ApiFile() {
        setType("file");
    }

    public void setOId(String id) {
        this.oId = id != null ? id.trim() : null;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public void setApplication(String application) {
        this.application = application != null
                ? application.trim() : null;
    }

    public void setExternalId(String id) {
        this.externalId = id != null ? id.trim() : null;
    }

    public void setClassName(String name) {
        this.className = name != null ? name.trim() : null;
    }

    @Override
    public String getDisplayName() {
        String displayName = getTitle();

        return displayName != null
                ? displayName
                : super.getDisplayName();
    }

    @JsonIgnore
    public List<ApiAttributeValues> getSeededAttributes() {
        return getAttributes();
    }

    @JsonProperty("seededAttributes")
    public void setSeededAttributes(List<ApiAttributeValues> values) {
        setAttributes(values);
    }

    public List<ICustomAttributeValue> getCustomAttributes() {
        if (customAttributes == null) {
            customAttributes = new ArrayList<>();
        }

        return customAttributes;
    }

    public void setCustomAttribute(String attribute, String value) {
        getCustomAttributes().add(
                new ICustomAttributeValue(attribute, value)
        );
    }

    public String getCustomAttributeValue(String attributeName) {
        for (ICustomAttributeValue value : getCustomAttributes()) {
            if (value.getAttribute().equalsIgnoreCase(attributeName)) {
                return value.getValue();
            }
        }

        return null;
    }

    public List<ICustomAttributeValue> getControlAttributes() {
        if (controlAttributes == null) {
            controlAttributes = new ArrayList<>();
        }

        return controlAttributes;
    }

    public void setControlAttribute(String attribute, String value) {
        getControlAttributes().add(
                new ICustomAttributeValue(attribute, value)
        );
    }

    public String getControlAttributeValue(String attributeName) {
        for (ICustomAttributeValue value : getControlAttributes()) {
            if (value.getAttribute().equalsIgnoreCase(attributeName)) {
                return value.getValue();
            }
        }

        return null;
    }

    public List<ApiResource> getResources() {
        if (resources == null) {
            resources = new ArrayList<>();
        }

        return resources;
    }

    public void addResource(ApiResource resource) {
        getResources().add(resource);
    }

    public List<ApiProxy> getRelationships() {
        if (relationships == null) {
            relationships = new ArrayList<>();
        }

        return relationships;
    }

    public void addRelation(ApiProxy relation) {
        getRelationships().add(relation);
    }
}
