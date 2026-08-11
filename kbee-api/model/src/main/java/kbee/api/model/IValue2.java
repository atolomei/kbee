package kbee.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class IValue2 extends ApiClassificable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String subline;
	private String href = null; // un value que actua como proxy
	private ApiProxy dataset;
	private String externalid;
	private List<ApiProxy> parents;
	
	private String firstName;
	private String lastName;
	private String email;
	
	private IFormData formData;


	public IValue2() {
		
	}
	
	public IValue2(String value) {
		setValue(value);
	}
	
	public String getValue() {
		return name;
	}
	
	public void setValue(String value) {
		this.name = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String value) {
		this.name = value;
	}
	
	public String getSubline() {
		return subline;
	}

	public void setSubline(String subline) {
		this.subline = subline;
	}

	public ApiProxy getDataSet() {
		return dataset;
	}
	
	public void setDataSet(ApiProxy dataset) {
		this.dataset = dataset;
	}
	
	public String getHRef() {
		return href;
	}
	
	public void setHRef(String value) {
		this.href = value;
	}
	
	public String getExternalId() {
		return externalid;
	}
	
	public void setExternalId(String id) {
		this.externalid = id;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String getDisplayName() {
		String name = getValue();
		if (name == null) name = getName();
		if (name == null) name = super.getDisplayName();
		return name;
	}

	public List<ApiProxy> getParents() {
		return parents;
	}

	public void setParents(List<ApiProxy> parents) {
		this.parents = parents;
	}

	public IFormData getFormData() {
		return formData;
	}

	public void setFormData(IFormData formData) {
		this.formData = formData;
	}
}
