package kbee.api.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.EXISTING_PROPERTY,
	    property = "type",
	    visible = true
	)
	@JsonSubTypes({
	    @JsonSubTypes.Type(value = ApiFile.class, name = "file"),
	    @JsonSubTypes.Type(value = ApiResource.class, name = "resource"),
	    @JsonSubTypes.Type(value = ApiValue.class, name = "value"),
	    @JsonSubTypes.Type(value = INode.class, name = "node")
	})
public class ApiObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String domain;
	private ApiProxy domainRef;
	private String displayName;
	private String state;
	private OffsetDateTime lastModifiedDate;
	private ApiProxy lastModifiedUser;
 	private String type;
    
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String id) {
		this.type = id;
	}

	
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domain) {
		this.domain = domain!=null?domain.trim():null;
	}
	
	public ApiProxy getDomainRef() {
		return domainRef;
	}
	
	public void setDomainRef(ApiProxy proxy) {
		this.domainRef = proxy;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String name) {
		this.displayName = (name!=null?name.trim():null);
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state!=null?state.trim():null;
	}
	
	public OffsetDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	public void setLastModifiedDate(OffsetDateTime date) {
		this.lastModifiedDate = date;
	}
	
	public ApiProxy getLastModifiedUser() {
		return lastModifiedUser;
	}
	
	public void setLastModifiedUser(ApiProxy user) {
		this.lastModifiedUser = user;
	}
}