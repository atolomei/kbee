package com.novamens.logging;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.content.base.Content;
import com.novamens.content.form.AbstractUpdatedField;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.content.form.ProcessLaunched;
import com.novamens.content.form.RelationAdded;
import com.novamens.content.form.ResourceAdded;
import com.novamens.content.form.ResourceMoved;
import com.novamens.content.form.ResourceRemoved;
import com.novamens.content.form.ResourceUpdated;
import com.novamens.content.form.ResourcesRemoved;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.form.ValueAdded;
import com.novamens.content.form.ValueRemoved;
import com.novamens.content.form.ValueUpdated;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

@Entity
@DiscriminatorValue("UpdateFormEvent")
public class UpdateFormEvent extends UpdateEvent {
	
	@Column(name = "EVENT_FORM_ID")
	private Long formId;
 
	static public String getClassEventType() {
		return "UpdateForm";
	}
	
	public UpdateFormEvent() {
	}
	
	public UpdateFormEvent(Content content, EForm form, List<UpdatedField> updatedFields) {
		super(content);
		setContent(content);
		setForm(form);
		setFields(updatedFields);
		setEventUser(ServiceLocator.getService(SecurityService.class).getSessionUser());
	}
	
	public void setForm(EForm form) {
		formId = form instanceof EIdentifiableForm ? (Long)((EIdentifiableForm)form).getId() : null;
	}
	
	public void setFields(List<UpdatedField> fields) {
		try {
			String json = getMapper().writeValueAsString(fields);
			setParameters(json);
		}
		catch (Exception e) {
		}
	}
	
	@Override
	public String getDescription() {
		
		StringBuilder description = new StringBuilder();
		
		
		try {
		
					
			List<AbstractUpdatedField> updates = getMapper().readValue(getParameters(), new TypeReference<List<AbstractUpdatedField>>(){});
			
			description.append("<ul>");
			
			UpdatedField lastupdate = null;
			boolean first = true;
			
			
			//StringBuilder row = new StringBuilder();
			
			for (UpdatedField update : updates) {
				
				//if (lastupdate==null || !lastupdate.getClass().equals(update.getClass())) {
				if (lastupdate==null || !lastupdate.same(update)) {
					
					if (lastupdate!=null) {
						description.append(" "+lastupdate.getAction());
						description.append("</li>");
					}
					
					description.append("<li>");
					
						
					//first = lastupdate!=null;
					first = true;
				}
				
				if (!first) 
					description.append(", ");
				
				description.append(update.getLabel());
				lastupdate = update;
				
				first = false;
				
				
				
			}
			
			
			if (lastupdate!=null) {
				
				description.append(" "+lastupdate.getAction());
				description.append("</li>");
			}
			description.append("</ul>");
		}
		catch (Exception e) {
			description.append( super.getDescription() + " | "  +e.getClass().getName());
		}
		return description.toString();
	}
	
	static private ObjectMapper mapper = new ObjectMapper();
	private ObjectMapper getMapper() {
	
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.registerSubtypes(ValueUpdated.class, 
			ValueAdded.class, 
			ValueRemoved.class, 
			RelationAdded.class, 
			ResourceAdded.class, 
			ResourceUpdated.class, 
			ResourceMoved.class,
			ProcessLaunched.class,
			ResourceRemoved.class,
			ResourcesRemoved.class);
		return mapper;
	}
}