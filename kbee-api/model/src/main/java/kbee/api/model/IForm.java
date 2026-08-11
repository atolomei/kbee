package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

public class IForm extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String displayLevel;
	private List<IComponent> components;
	private boolean fileContainer;
	private String viewer;
	private ApiProxy template;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public ApiProxy getTemplate() {
		return template;
	}

	public void setTemplate(ApiProxy template) {
		this.template = template;
	}

	public List<IComponent> getComponents() {
		return components;
	}

	public void setComponents(List<IComponent> components) {
		this.components = components;
	}
	
	public void addComponent(IComponent component) {
		if (components == null) components = new ArrayList<IComponent>();			
		components.add(component);
	}

	public String getDisplayLevel() {
		return displayLevel;
	}

	public void setDisplayLevel(String displayLevel) {
		this.displayLevel = displayLevel;
	}

	public boolean isFileContainer() {
		return fileContainer;
	}

	public void setFileContainer(boolean fileContainer) {
		this.fileContainer = fileContainer;
	}

	public String getViewer() {
		return viewer;
	}

	public void setViewer(String viewer) {
		this.viewer = viewer;
	}
}