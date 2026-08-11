package kbee.api.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiFile extends ApiClassificable {
	private static final long serialVersionUID = 1L;
	
	private int version;
	private String oid;
	private String title;
	private String subtitle;
	private String application;
	private String externalid;
	private String classname;
	private ApiProxy contentClass;
	private List<ApiProxy> relationships;
	private ApiProxy previousVersion;
	private ApiProxy workspace;
	
	private List<ApiResource> resources = new ArrayList<>();
	private List<ICustomAttributeValue> customattributes = null;
	private List<ICustomAttributeValue> controlattributes = null;
	
	private List<IFormData> forms;
	
	public ApiFile() {
		setType("file");
	}
	
	public String getOId() {
		return oid;
	}
	
	public void setOId(String id) {
		this.oid = id!=null?id.trim():null;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title!=null?title.trim():null;
	}
	
	
	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@Override
	public String getDisplayName() {
		String displayName = getTitle();
		if (displayName==null) 
			displayName = super.getDisplayName();
		return displayName;
	}
	 
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public String getApplication() {
		return application;
	}
	
	public void setApplication(String name) {
		this.application = name!=null?name.trim():null;
	}
	
	public String getExternalId() {
		return externalid;
	}
	
	public void setExternalId(String id) {
		this.externalid = (id!=null?id.trim():null);
	}
	
	public ApiProxy getContentClass() {
		return contentClass;
	}
	
	public void setContentClass(ApiProxy template) {
		this.contentClass = template;
	}
	
	public void setClassName(String name) {
		this.classname = (name!=null?name.trim():null);
	}
	
	public String getClassName() {
		return classname;
	}
	
	public ApiProxy getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(ApiProxy workspace) {
		this.workspace = workspace;
	}
	
	public ApiProxy getPreviousVersion() {
		return previousVersion;
	}
	
	public void setPreviousVersion(ApiProxy file) {
		this.previousVersion = file;
	}
	
	public List<IAttributeValues> getSeededAttributes() {
		return getAttributes();
	}
	
	public void setSeededAttributes(List<IAttributeValues> values) {
		setAttributes(values);
	}
	
	public List<ICustomAttributeValue> getCustomAttributes() {
		if (this.customattributes == null)
			this.customattributes = new ArrayList<ICustomAttributeValue>();
		return customattributes;
	}
	
	public void setCustomAttribute(String attribute, String value) {
		if (this.customattributes == null)
			this.customattributes = new ArrayList<ICustomAttributeValue>();
		this.customattributes.add(new ICustomAttributeValue(attribute, value));
	}
	
	public String getCustomAttributeValue(String attributename) {
		if (getCustomAttributes()!=null)
		for (ICustomAttributeValue value : getCustomAttributes()) {
			if (value.getAttribute().toLowerCase().equals(attributename.toLowerCase())) {
				return value.getValue();
			}
		}
		return null;
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
	
	public String getControlAttributeValue(String attributename) {
		if (getControlAttributes()!=null)
		for (ICustomAttributeValue value : getControlAttributes()) {
			if (value.getAttribute().toLowerCase().equals(attributename.toLowerCase())) {
				return value.getValue();
			}
		}
		return null;
	}
	
	public List<ApiResource> getResources() {
		if (this.resources==null)
			resources = new ArrayList<ApiResource>();
		return resources;
	}
	
	public void addResource(ApiResource resource) {
		this.resources.add(resource);
	}
	
	public void setResources(List<ApiResource> files) {
		this.resources = files;
	}
	
	public void setRelationships(List<ApiProxy> relations) {
		this.relationships = relations;
	}
	
	public List<ApiProxy> getRelationships() {
		if (this.relationships==null)
			relationships = new ArrayList<ApiProxy>();
		return this.relationships;
	}
	
	public void addRelation(ApiProxy relation) {
		if (this.relationships==null)
			relationships = new ArrayList<ApiProxy>();
		this.relationships.add(relation);
	}
	
	public List<IFormData> getForms() {
		return forms;
	}

	public void setForms(List<IFormData> forms) {
		this.forms = forms;
	}
}
