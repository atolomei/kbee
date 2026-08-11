package kbee.api.model;

public class IModelAttribute extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String alias;
	private String multiplicity;
	private String predicate;
	private String uniquename;
	private boolean filterable;
	
	public String getMultiplicity() {
		return multiplicity;
	}
	
	public void setMultiplicity(String multiplicity) {
		this.multiplicity = multiplicity;
	}
	
	public String getUniqueName() {
		return uniquename;
	}
	
	public void setUniqueName(String name) {
		this.uniquename = name;
	}
	
	public String getPredicate() {
		return predicate;
	}
	
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isFilterable() {
		return filterable;
	}

	public void setFilterable(boolean filterable) {
		this.filterable = filterable;
	}
}
