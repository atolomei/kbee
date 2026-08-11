package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * 
 * TODO: 
 
 * - fileName
 * - mimeType
 * - size
 * 
 */
@JsonInclude(Include.NON_NULL)
public class ApiResource extends ApiObject {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String title;
	private String description;
	private String href;
	private String rel;
	private String crc;
	private ApiProxy tag;
	
	private List<ICustomAttributeValue> controlattributes = null;
	
	public ApiResource() {
	}
	
	public ApiResource(String href) {
		setHRef(href);
	}
	
	public ApiResource(String href, String name) {
		setHRef(href);
		setName(name);
	}
	
	public String getDisplayName() {
		return super.getDisplayName()!=null?super.getDisplayName():getName();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getHRef() {
		return href;
	}
	
	public void setHRef(String href) {
		this.href = href;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String rel) {
		this.title = rel;
	}
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String rel) {
		this.description = rel;
	}
	
	public String getRel() {
		return rel;
	}
	
	public void setRel(String rel) {
		this.rel = rel;
	}
 	
	public String getCRC() {
		return crc;
	}
	
	public void setCRC(String crc) {
		this.crc = crc;
	}
	
	public ApiProxy getTag() {
		return tag;
	}

	public void setTag(ApiProxy tag) {
		this.tag = tag;
	}

	public void setControlAttribute(String attribute, String value) {
		if (this.controlattributes==null)
			controlattributes = new ArrayList<ICustomAttributeValue>();
		this.controlattributes.add(new ICustomAttributeValue(attribute, value));
	}
	
	public List<ICustomAttributeValue> getControlAttributes() {
		if (this.controlattributes==null)
			controlattributes = new ArrayList<ICustomAttributeValue>();
		return controlattributes;
	}
	
	public String getControlAttributeValue(String name) {
		for (ICustomAttributeValue value : getControlAttributes()) {
			if (value.getAttribute().toLowerCase().equals(name.toLowerCase())) {
				return value.getValue();
			}
		}
		return null;
	}
}