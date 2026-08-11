package kbee.api.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class IFieldData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String label;
	private String type;
	private List<IFieldValue> values;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public List<IFieldValue> getValues() {
		return values;
	}
	
	public void setValues(List<IFieldValue> values) {
		this.values = values;
	}
}