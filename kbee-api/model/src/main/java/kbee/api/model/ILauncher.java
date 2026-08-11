package kbee.api.model;

public class ILauncher extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private boolean newDocumentEnabled;
	private boolean libraryEnabled;
	private boolean apiEnabled;
	private boolean mobile;
	private IAcl acl;
	private ApiProxy group;
	private ApiProxy procedure;
	private ApiProxy template;
	private String description;

	public ApiProxy getProcedure() {
		return procedure;
	}
	
	public void setProcedure(ApiProxy procedure) {
		this.procedure = procedure;
	}
	
	public boolean isNewDocumentEnabled() {
		return newDocumentEnabled;
	}
	
	public void setNewDocumentEnabled(boolean newDocumentsEnabled) {
		this.newDocumentEnabled = newDocumentsEnabled;
	}
	
	public boolean isLibraryEnabled() {
		return libraryEnabled;
	}
	
	public void setLibraryEnabled(boolean libraryEnabled) {
		this.libraryEnabled = libraryEnabled;
	}

	public boolean isApiEnabled() {
		return apiEnabled;
	}

	public void setApiEnabled(boolean apiEnabled) {
		this.apiEnabled = apiEnabled;
	}

	public boolean isMobile() {
		return mobile;
	}

	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}

	public IAcl getAcl() {
		return acl;
	}

	public void setAcl(IAcl acl) {
		this.acl = acl;
	}

	public ApiProxy getGroup() {
		return group;
	}

	public void setGroup(ApiProxy group) {
		this.group = group;
	}

	public ApiProxy getTemplate() {
		return template;
	}

	public void setTemplate(ApiProxy template) {
		this.template = template;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}