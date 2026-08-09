package kbee.web.eform;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormResourceEvent;
import com.novamens.kbee.wicket.editor.Editor;

public class EAjaxFormResourceAddedEvent extends EAjaxFormEvent implements EFormResourceEvent {
	
	Resource resource;
	ResourceTag tag;
	Editor<?> editor;
	
	public EAjaxFormResourceAddedEvent(AjaxRequestTarget target) {
		super(target);
	}
	
	public EAjaxFormResourceAddedEvent(AjaxRequestTarget target, Editor<?> editor, EFormData data, Resource resource, ResourceTag tag) {
		super(target, null, data);
		setEditor(editor);
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

	public Editor<?> getEditor() {
		return editor;
	}

	public void setEditor(Editor<?> editor) {
		this.editor = editor;
	}
}