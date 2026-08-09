package kbee.web.eform;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EFormResourceEvent;

public class EAjaxFormResourceEvent extends EAjaxFormEvent implements EFormResourceEvent {
	
	Resource resource;
	ResourceTag tag;
	
	public EAjaxFormResourceEvent(AjaxRequestTarget target) {
		super(target);
	}
	
	public EAjaxFormResourceEvent(AjaxRequestTarget target, EFormField<?> field) {
		super(target, field);
	}
	
	public EAjaxFormResourceEvent(AjaxRequestTarget target, EFormField<?> field, EFormData data, Resource resource, ResourceTag tag) {
		super(target, field, data);
		setResource(resource);
		setTag(tag);
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public ResourceTag getTag() {
		return tag;
	}

	public void setTag(ResourceTag tag) {
		this.tag = tag;
	}
	
}