package kbee.api.model;

public class ILauncherGroup extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String alias;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}	
}
