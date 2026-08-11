package kbee.api.model;

import java.io.Serializable;

public class IModelElement implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String mutiplicity;
	private ApiProxy attribute;
	private ApiProxy parent;

	public ApiProxy getParent() {
		return parent;
	}

	public void setParent(ApiProxy parent) {
		this.parent = parent;
	}

	public ApiProxy getAttribute() {
		return attribute;
	}

	public void setAttribute(ApiProxy attribute) {
		this.attribute = attribute;
	}

	public String getMutiplicity() {
		return mutiplicity;
	}

	public void setMutiplicity(String mutiplicity) {
		this.mutiplicity = mutiplicity;
	}
}