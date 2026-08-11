package kbee.api.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ApiWorkflowContext implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ApiFile file;
	private Map<String,String> parameters = new HashMap<>();;
	
	public String getParameter(String name) {
		return parameters.get(name);
	}
	
	public void setParameter(String name, String value) {
		parameters.put(name, value);
	}
}