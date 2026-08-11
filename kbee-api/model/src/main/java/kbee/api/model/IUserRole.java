package kbee.api.model;

import java.io.Serializable;

public class IUserRole implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private ApiProxy role;
	private ApiProxy entity;
	
	public IUserRole() {
		
	}
	
	public IUserRole(ApiProxy role, ApiProxy entity) {
		setRole(role);
		setEntity(entity);
	}
	
	public ApiProxy getRole() {
		return role;
	}
	
	public void setRole(ApiProxy role) {
		this.role = role;
	}
	
	public ApiProxy getEntity() {
		return entity;
	}
	
	public void setEntity(ApiProxy role) {
		this.entity = role;
	}

}
