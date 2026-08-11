package com.novamens.content.model;


public interface ExternalMember extends DataSetMember {

	public void setExternalMemberId(Long id);
	public Long getExternalMemberId();
	
	public String getExternalUrl();

	
}
