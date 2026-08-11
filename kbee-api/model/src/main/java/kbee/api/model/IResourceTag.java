package kbee.api.model;

public class IResourceTag extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private boolean multiple;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isMultiple() {
		return multiple;
	}
	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}
	
}
