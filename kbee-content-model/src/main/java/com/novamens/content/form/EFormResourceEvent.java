package com.novamens.content.form;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;

public interface EFormResourceEvent extends EFormEvent {
 	public Resource getResource();
	public ResourceTag getTag();
}