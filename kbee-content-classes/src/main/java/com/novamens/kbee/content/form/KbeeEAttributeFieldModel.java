package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.form.EAttributeModel;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElement;
import com.novamens.kbee.content.model.KbeeAttribute;
import com.novamens.service.ServiceLocator;

public abstract class  KbeeEAttributeFieldModel<T> extends KbeeEModelElementFieldModel<T> implements EAttributeModel<T> {
	
	private static final long serialVersionUID = 1L;
	
	private String attributeId;
	
	@JsonProperty("attribute")
	private String attributeName;
	
	@Override
	@JsonIgnore
	public ModelElement getElement() {
		return getAttribute();
	}
	
	public void setAttribute(Attribute attribute) {
		this.attributeId = String.valueOf(attribute.getId());
	}
	
	public void setAttribute(String attributeId) {
		this.attributeId = attributeId;
	}
	
	public String getAttributeId() {
		return this.attributeId;
	}
	
	public void setAttributeName(String attributeId) {
		this.attributeName = attributeId;
	}
	
	public String getAttributeName() {
		return this.attributeName;
	}
	
	@JsonIgnore
	public Attribute getAttribute() {
		if (attributeId!=null) {
			SessionFactory sf = (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
			Attribute attribute = null;
			try {
				attribute = (Attribute)sf.getCurrentSession().load(KbeeAttribute.class, Long.valueOf(this.attributeId));
			}
			catch (ObjectNotFoundException e) {
				
			}
			return attribute;
		}
		else {
			if (attributeName!=null) {
				for (Attribute attribute : getContentDao().getAttributes(getContentDao().getDomain())) {
					if (attributeName.equals(attribute.getAlias())) {
						attributeId = String.valueOf(attribute.getId());
						return attribute;
					}
				}
			}
		}
		return null;
	}
	
	// Update object with data
	@Override
	public void set(Object object, List<T> data) {
		Assert.isInstanceOf(Classificable.class, object);
		List<String> values = new ArrayList<String>();
		if (data!=null) data.forEach( value -> values.add(toString(value)));
		((Classificable)object).setAttributeValues(getAttribute(), values);
	}
	
	@Override
	public void set(Object object, Object data) {
		Assert.isInstanceOf(Classificable.class, object);
		List<String> values = new ArrayList<String>();
		if (data!=null) values.add(toString(data));
		((Classificable)object).setAttributeValues(getAttribute(), values);
	}
	
	// get data from object
	@Override
	public T get(Object object) {
		Assert.isInstanceOf(Classificable.class, object);
		List<String> values = ((Classificable)object).getAttributeValues(getAttribute());
		String stringvalue = values.isEmpty() ? null : values.get(0);
		T value = stringvalue!=null ? 
			getValueOf(stringvalue) : 
			null;
		return value;
	}
	
	public List<T> getValues(Object object) {
		Assert.isInstanceOf(Classificable.class, object);
		List<T> values = new ArrayList<T>();
		for (String stringvalue : ((Classificable)object).getAttributeValues(getAttribute())) {
			values.add(getValueOf(stringvalue));
		}
		return values;
	}
	
	@Override
	public T deserialize(Classificable formobject, String token) {
		return token!=null ? getValueOf(token) : null;
	}
	
	@Override
	public String getErrorMessage(Object object) {
		
		StringBuilder message = new StringBuilder();
		
		if (getAttribute()==null) {
			message.append(getModelObjectName()+" ");
			message.append(attributeName!=null ? attributeName : attributeId);
			message.append(" not found");
			return message.toString();
		}
		
		if (object!=null && (object instanceof Content)) {
			boolean found = false;
			ContentTemplate template = ((Content)object).getContentTemplate();
			for (AttributeTemplate attributetemplate : template.getAttributes()) {
				if (attributetemplate.getAttribute()!=null && attributetemplate.getAttribute().equals(getAttribute())) {
					found = true;
					break;
				}
			}
			if (!found) {
				message.append(getModelObjectName()+" ");
				message.append(attributeName!=null ? attributeName : attributeId);
				message.append(" not found in "+ template.getDisplayName() + " template");
				return message.toString();
			}
		}
		return super.getErrorMessage(object);
	}
	

	@JsonIgnore
	public String getModelObjectName(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle( KbeeEAttributeFieldModel.class.getName(), locale);
		return res.getString("attribute");
	}

	 
	
	@Override
	@JsonIgnore
	public String getDescription(Locale locale) {
		
		StringBuilder description = new StringBuilder();
		
		if (getParentClassifier()!=null) {
			description.append(getParentClassifier().getDisplayName() + "->");
		}
		description.append(getAttribute()!=null ? getAttribute().getDisplayName() : " not found");
		description.append(" ( " + getModelObjectName(locale) + " )");
		return description.toString();
	}
	
	@Override
	@JsonIgnore
	public String getTypeLabel() {
		return EAttributeModel.GetTypeLabel();
	}
	
	@Override
	protected List<T> getValuesFrom(DataSetMember parentValue) {
		List<T> values = new ArrayList<T>();
		for (String stringvalue : parentValue.getAttributeValues(getAttribute())) {
			values.add(getValueOf(stringvalue));
		}
		return values;
	}
	
	protected abstract String toString(Object value);
	
	protected abstract T getValueOf(String value);
}