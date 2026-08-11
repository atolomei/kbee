package kbee.api.model;

public class ApiClassifier extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private ApiProxy dataset;
	private ApiProxy dataset2;
	private String multiplicity;
	private String alias;
	private boolean rules;
	private boolean contentType;
	private boolean searchable;
	private String predicate;
	private String uniquename;
	
	public ApiProxy getDataSet() {
		return dataset;
	}
	
	public void setDataSet(ApiProxy dataset) {
		this.dataset = dataset;
	}
	
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

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public ApiProxy getDataSet2() {
		return dataset2;
	}

	public void setDataSet2(ApiProxy dataset2) {
		this.dataset2 = dataset2;
	}

	public boolean isRules() {
		return rules;
	}

	public void setRules(boolean rules) {
		this.rules = rules;
	}

	public boolean isContentType() {
		return contentType;
	}

	public void setContentType(boolean contentType) {
		this.contentType = contentType;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}
}
