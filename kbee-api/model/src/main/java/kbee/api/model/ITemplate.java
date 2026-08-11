package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class ITemplate extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String baseClass;
	private String titleRule;
	private String portalSubline;
	private String consoleSubline;
	private boolean titleEditable;
	private boolean onlyRoot;
	private List<IModelElement> structure;
	private List<ApiProxy> procedures;
	private List<ApiProxy> forms;
	private List<ApiProxy> resourceTags;
	
	public List<ApiProxy> getProcedures() {
		return procedures;
	}
	public void setProcedures(List<ApiProxy> procedures) {
		this.procedures = procedures;
	}
	public void addProcedure(ApiProxy procedure) {
		if (procedures == null) procedures = new ArrayList<>();			
		procedures.add(procedure);
	}
	public List<IModelElement> getStructure() {
		return structure;
	}
	public void setStructure(List<IModelElement> structure) {
		this.structure = structure;
	}
	public void addStructure(IModelElement element) {
		if (structure == null) structure = new ArrayList<IModelElement>();			
		structure.add(element);
	}
	public String getBaseClass() {
		return baseClass;
	}
	public void setBaseClass(String name) {
		this.baseClass = name;
	}
	public List<ApiProxy> getForms() {
		return forms;
	}
	public void setForms(List<ApiProxy> forms) {
		this.forms = forms;
	}
	public void addForm(ApiProxy form) {
		if (forms == null) forms = new ArrayList<ApiProxy>();			
		forms.add(form);
	}
	public boolean isTitleEditable() {
		return titleEditable;
	}
	public void setTitleEditable(boolean titleEditable) {
		this.titleEditable = titleEditable;
	}
	public List<ApiProxy> getResourceTags() {
		return resourceTags;
	}
	public void setResourceTags(List<ApiProxy> resourceTags) {
		this.resourceTags = resourceTags;
	}
	public void addResourceTag(ApiProxy tag) {
		if (resourceTags == null) resourceTags = new ArrayList<ApiProxy>();			
		resourceTags.add(tag);
	}
	public String getTitleRule() {
		return titleRule;
	}
	public void setTitleRule(String titleRule) {
		this.titleRule = titleRule;
	}
	public boolean isOnlyRoot() {
		return onlyRoot;
	}
	public void setOnlyRoot(boolean onlyRoot) {
		this.onlyRoot = onlyRoot;
	}
	public String getPortalSubline() {
		return portalSubline;
	}
	public void setPortalSubline(String portalSubline) {
		this.portalSubline = portalSubline;
	}
	public String getConsoleSubline() {
		return consoleSubline;
	}
	public void setConsoleSubline(String consoleSubline) {
		this.consoleSubline = consoleSubline;
	}
	
}