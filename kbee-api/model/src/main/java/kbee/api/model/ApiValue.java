package kbee.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class ApiValue extends ApiClassificable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String subline;
    private String hRef;
    private ApiProxy dataSet;
    private String externalId;
    private ApiProxy parent;
    private List<ApiProxy> path;
    private ApiProxy person;

    private String firstName;
    private String lastName;
    private String email;

    private ApiFormData formData;

    public ApiValue() {
        setType("value");
    }

    public ApiValue(String value) {
        setValue(value);
    }

    public String getValue() {
        return name;
    }

    public void setValue(String value) {
        this.name = value;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonIgnore
    @Override
    public String getDisplayName() {
        String name = getValue();

        if (name == null) {
            name = getName();
        }

        if (name == null) {
            name = super.getDisplayName();
        }

        return name;
    }
}
