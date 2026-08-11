package kbee.api.model;

public class ApiDomain extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;

	public ApiDomain() {
	}

	public ApiDomain(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}