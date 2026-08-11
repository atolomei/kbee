package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;


// REGLA PARA FECHA DEL DIA ${.now?string('yyyy-MM-dd')}
public class KbeeAttributeRule implements AttributeRule, Serializable {
	private static final long serialVersionUID = 1L;

	private Serializable attributeId;
	private String value;
	
	public KbeeAttributeRule() {
		
	}
	
	public KbeeAttributeRule(Attribute attribute, String value) {
		setAttribute(attribute);
		setValue(value);
	}
	
	public void execute(WorkflowContext context) {
		KbeeTextTemplate template = new KbeeTextTemplate(this.value);
		String value = template.process(((KbeeContext) context).getContent());
		ObjectModel<Content> model = new ObjectModel<Content>(((KbeeContext)context).getContent());
		if (value!=null) {
			List<String> values = new ArrayList<String>();
			values.add(value);
			model.getObject().setAttributeValues(getAttribute(), values);
		}
	}
	
	public Attribute getAttribute() {
		if (attributeId==null) return null;
		SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
		Attribute attribute = (Attribute)sf.getCurrentSession().load(KbeeAttribute.class, this.attributeId);
		return attribute;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attributeId = attribute!=null ? attribute.getId() : null;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getDescription() {
		
		StringBuilder description = new StringBuilder();
		
		if (getAttribute()==null || getValue()==null) 
			return null;
		description.append(getAttribute().getDisplayName());
		description.append(" -> ");
		description.append(getValue());
		return description.toString();
	}
}