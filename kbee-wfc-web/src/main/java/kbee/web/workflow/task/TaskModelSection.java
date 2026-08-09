package kbee.web.workflow.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;

@Deprecated
public class TaskModelSection implements ModelSection, Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return null;
	}
	
	public List<ModelElementTemplate> getStructure() {
		return structure;
	}
	
	public void setStructure(List<ModelElementTemplate> structure) {
		this.structure = structure;
	}

	@Override
	public boolean isPortal() {
		// TODO Auto-generated method stub
		return false;
	}
}	