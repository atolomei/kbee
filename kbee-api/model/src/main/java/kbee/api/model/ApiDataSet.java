package kbee.api.model;

import java.util.List;

/**
 * @author aferr
 *
 */
public class ApiDataSet extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String subtype;
	private String alias;
	private String displayNameRule;
	private String sublineRule;
	private boolean displayNameEditable;
	private boolean hierachical;
	private List<IModelElement> structure;
	private long size;
	
	public String getType() {
		return type;
	}
	
	public void setType(String id) {
		this.type = id;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String id) {
		this.alias = id;
	}
		
	public String getDisplayNameRule() {
		return displayNameRule;
	}

	public void setDisplayNameRule(String titleRule) {
		this.displayNameRule = titleRule;
	}

	public boolean isDisplayNameEditable() {
		return displayNameEditable;
	}

	public String getSublineRule() {
		return sublineRule;
	}

	public void setSublineRule(String sublineRule) {
		this.sublineRule = sublineRule;
	}

	public void setDisplayNameEditable(boolean titleEditable) {
		this.displayNameEditable = titleEditable;
	}

	public List<IModelElement> getStructure() {
		return structure;
	}
	
	public void setStructure(List<IModelElement> structure) {
		this.structure = structure;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	public boolean isHierachical() {
		return hierachical;
	}

	public void setHierachical(boolean hierachical) {
		this.hierachical = hierachical;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
