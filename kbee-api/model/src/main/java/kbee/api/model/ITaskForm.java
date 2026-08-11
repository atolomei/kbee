package kbee.api.model;

public class ITaskForm extends ApiProxy {
	private static final long serialVersionUID = 1L;
	
	private boolean signatureRequired; 
	private boolean readonly;
	private String layout;
	
	public ITaskForm() {
	}
	
	public ITaskForm(String id, String name, String uri) {
		super(id, name, uri, "eform");
	}

	public boolean isSignatureRequired() {
		return signatureRequired;
	}

	public void setSignatureRequired(boolean signatureRequired) {
		this.signatureRequired = signatureRequired;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}
}
