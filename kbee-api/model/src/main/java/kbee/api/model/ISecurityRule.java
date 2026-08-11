package kbee.api.model;

public class ISecurityRule extends ApiObject {
	private static final long serialVersionUID = 1L;
	private String condition;
	private String description;
	private IAcl acl;
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String expression) {
		this.condition = expression;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setAcl(IAcl acl) {
		this.acl = acl;
	}
	
	public IAcl getAcl() {
		return this.acl;
	}
}