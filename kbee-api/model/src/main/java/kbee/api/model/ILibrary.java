package kbee.api.model;

public class ILibrary extends ApiObject {
	private static final long serialVersionUID = 1L;

	private String name;
	private String criteria;
	private boolean canonical;
	private ApiProxy readers;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCriteria() {
		return criteria;
	}
	
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	public boolean isCanonical() {
		return canonical;
	}
	
	public void setCanonical(boolean canonical) {
		this.canonical = canonical;
	}

	public ApiProxy getReaders() {
		return readers;
	}

	public void setReaders(ApiProxy readers) {
		this.readers = readers;
	}
	
	
}