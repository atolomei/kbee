package kbee.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IComponent implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String name;
	private String label;
	private String sublabel;
	private String help;
	private String model;
	private String css;
	private String text;
	private String calculation;
	private String visible;
	private String enabled;
	private ApiProxy classifier;
	private ApiProxy attribute;
	private ApiProxy resourceTag;
	private ApiProxy relation;

	private List<IComponent> childs;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

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

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public ApiProxy getClassifier() {
		return classifier;
	}

	public void setClassifier(ApiProxy classifier) {
		this.classifier = classifier;
	}

	public ApiProxy getAttribute() {
		return attribute;
	}

	public void setAttribute(ApiProxy attribute) {
		this.attribute = attribute;
	}

	public ApiProxy getResourceTag() {
		return resourceTag;
	}

	public void setResourceTag(ApiProxy resourceTag) {
		this.resourceTag = resourceTag;
	}

	public ApiProxy getRelation() {
		return relation;
	}

	public void setRelation(ApiProxy relation) {
		this.relation = relation;
	}
	
	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getSublabel() {
		return sublabel;
	}

	public void setSublabel(String sublabel) {
		this.sublabel = sublabel;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String getCalculation() {
		return calculation;
	}

	public void setCalculation(String calculation) {
		this.calculation = calculation;
	}

	public String getVisible() {
		return visible;
	}

	public void setVisible(String visible) {
		this.visible = visible;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public List<IComponent> getChilds() {
		return childs;
	}

	public void setChilds(List<IComponent> childs) {
		this.childs = childs;
	}
	
	public void addChild(IComponent component) {
		if (childs == null) childs = new ArrayList<IComponent>();			
		childs.add(component);
	}
}
