package kbee.api.model;

import java.util.List;

public class IFacet extends ApiObject {
	private static final long serialVersionUID = 1L;

	private String name;
	private List<IKeyValue> visibility;
	private List<IMember> values;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public List<IKeyValue> getVisibility() {
		return visibility;
	}

	public void setVisibility(List<IKeyValue> visibility) {
		this.visibility = visibility;
	}

	public List<IMember> getValues() {
		return values;
	}

	public void setValues(List<IMember> values) {
		this.values = values;
	}

}