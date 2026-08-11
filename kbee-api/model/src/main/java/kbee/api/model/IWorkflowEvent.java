package kbee.api.model;

import java.util.HashMap;
import java.util.Map;

public class IWorkflowEvent extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String activity;
	private String name;
	private String note;
	private String collaborator;
	Map<String,String> parameters = new HashMap<String, String>();;
	
	private IDevice device;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNote() {
		return note;
	}
	
	public void setNote(String note) {
		this.note = note;
	}
	
	public String getCollaborator() {
		return collaborator;
	}
	
	public void setCollaborator(String collaborator) {
		this.collaborator = collaborator;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public IDevice getDevice() {
		return device;
	}

	public void setDevice(IDevice device) {
		this.device = device;
	}
	
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}