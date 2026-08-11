package com.novamens.content.base;

import com.novamens.content.security.IQLRule;


public interface SiteIQLRule extends IQLRule {
	
	public String getRelatedObjectId();
	public void setRelatedObjectId(String id);
	

}
