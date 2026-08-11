package kbee.api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class INode extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private Boolean hasChilds;
	private List<INode> breadCrumb;

	public INode() {
		setType("node");
	}
	
	public void setHasChilds(boolean value) {
		hasChilds = value;;
	}
	
	public Boolean getHasChilds() {
		return hasChilds;
	}
	
	public List<INode> getBreadCrumb() {
		return breadCrumb;
	}
	
	public void setBreadCrumb(List<INode> nodes) {
		breadCrumb = nodes;
	}
}
