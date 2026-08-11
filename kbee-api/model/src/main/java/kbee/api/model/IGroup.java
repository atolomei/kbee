package kbee.api.model;

public class IGroup extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private boolean canonical = false;
	private String area;
	
	
	public String getName() {
		return getDisplayName();
	}
	
	public void setName(String name) {
		setDisplayName(name);
	}
	
	public boolean isCanonical() {
		return canonical;
	}
	
	public void setCanonical(boolean value) {
		this.canonical = value;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}
	
}
