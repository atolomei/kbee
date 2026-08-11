package com.novamens.kbee.content.service;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.text.template.Variable;
import com.novamens.content.text.template.VariableResolver;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class SimpleContentVariableResolver<T extends Content> implements VariableResolver {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SimpleContentVariableResolver.class.getName());
	
	
	private T content;
	

	public SimpleContentVariableResolver(T content) {
		this.content = content;
	}
	
	public String getValue(Variable variable) {
		
		try {
			if (variable.getType().equals("classifier"))				return getClassification(variable);
			if (variable.getType().equals("attribute")) 				return getAttribute(variable);
			if (variable.getName().equals("date"))						return getDate(variable);
			if (variable.getName().equals("time"))						return getTime();
		} 
		catch (Exception e) {
			logger.error(e);
			return "";
		}
		
		return "_";
	}
	
	public String getClassification(Variable variable) {
		
		String classifiername, attributename, value = "_";
		
		classifiername = variable.getName();
		
		Object object = getClassification(classifiername);
		
		if (object instanceof Date) {
			try {
				if (variable.getFormat()!=null) {
					SimpleDateFormat format = new SimpleDateFormat(variable.getFormat());
					value = format.format((Date)object);
					return value;
				}
				else {
					return ((Date)object).toString();
				}	
			}
			catch (Exception e) {
				logger.error(e);
				return "_";
			}
		}
		if (object instanceof OffsetDateTime) {
			if (variable.getFormat()!=null) {
				String s = variable.getFormat();
				s=s.replace("MMMM ", "MMM ");
				DateTimeFormatter f = DateTimeFormatter.ofPattern(s);
				value = f.format((OffsetDateTime)object);
				return value;
			}
			else {
				return ((OffsetDateTime)object).toString();
			}	
		}
		
		DataSetMember member = null;
		
		if (object instanceof DataSetMember) {
			member = (DataSetMember)object;
		}
		if (member==null) {
			return "_";
		}
		attributename = variable.getAttribute();
		if (attributename!=null) {
			value = getAttribute(member, attributename);
		}
		else {
			value = member.getDisplayName();
		}
		if (value==null) {
			return "_";
		}
		return value;
	}
	
	
	
	public String getAttribute(Variable variable) {
		
		String value = null;
		Attribute attribute = null;
		
		for (AttributeTemplate template : getContent().getContentTemplate().getAttributes()) {
			if (template.getAttribute().getName().toLowerCase().equals(variable.getName().toLowerCase())) {
				attribute = template.getAttribute();
				break;
			}
		}
		
		if (attribute!=null) {
			List<String> values = getAttributeValue(attribute);
			if (values!=null && !values.isEmpty()) {
				value = values.get(0);
			}
			else
				return null;
		}
		
		if (variable.getFormat()!=null && attribute.getType().equals(AttributeType.DATE)) {
			try {
				LocalDateTime date =  LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
				DateTimeFormatter formatter= DateTimeFormatter.ofPattern(variable.getFormat());
				value = formatter.format(date);
			}
			catch (Exception e) {
				return null;
			}
		}
		
		if (variable.getFormat()!=null && "capital".equals(variable.getFormat().toLowerCase())) {
			if (value.length()>1) {
				value = value.substring(0,1).toUpperCase() + value.substring(1).toLowerCase();
			}
			else {
				value = value.toUpperCase();
			}
		}
		return value;
	}
	
	public T getContent() {
		return content;
	}
	
	
	
	
	/**
	 * TODO: pasar a DateTimeService
	 * 
	 * @param variable
	 * @return
	 */
	public String getDate(Variable variable) {
		Date date = new Date();
		String st;
		if (getSessionUser().getLocale().getLanguage().equals("es"))
			st="dd MMMM YYYY";
		else
			st="MMMM dd YYYY";
		DateFormat format = variable.getFormat()!=null ? new SimpleDateFormat(variable.getFormat()) : new SimpleDateFormat(st, getSessionUser().getLocale());
		String formated = format.format(date);
		return formated;
	}
	
	
	
	
	
	/**
	 * TODO: pasar a DateTimeService
	 */
	public String getTime() {
		Date date = new Date();
		DateFormat format;
		if (getSessionUser().getLocale().getLanguage().equals("es"))
			format = new SimpleDateFormat("dd/MM/YYYY hh:mm:ss");
		else
			format = new SimpleDateFormat("MM/dd/YYYY hh:mm:ss");
		String formated = format.format(date);
		return formated;
	}


	
	protected List<Classification> getClassification() {
		return  getContent().getClassification();
	}
	
	protected List<String> getAttributeValue(Attribute attribute) {
		List<String> values = getContent().getAttributeValues(attribute);
		return values;
	}
	
	private Object getClassification(String classifiername) {
		for (Classification classification : getClassification()) {
			if (classification!=null && classification.getClassifier().getName().toLowerCase().equals(classifiername.toLowerCase())) {
				if (classification.getDataSetType().equals(DataSetType.DATE))
					return classification.getDateValue()!=null ? classification.getDateValue() : classification.getDataSetMember().getDateValue();
				else	
					return classification.getDataSetMember();
			}
		}
		return null;
	}
	
	private String getAttribute(DataSetMember member, String attribute) {
		for (AttributeTemplate template : member.getDataSet().getAttributes()) {
			if (template.getAttribute().getName().toLowerCase().equals(attribute.toLowerCase())) {
				List<String> values = member.getAttributeValues(template.getAttribute());
				if (values.isEmpty())
					return null;
				else
					return values.get(0);
			}
		}
		if ("email".equals(attribute) || "phone".equals(attribute)) {
			return getPersonAttribute(member, attribute);
		}
		return null;
	}
	
	private String getPersonAttribute(DataSetMember member, String attribute) {
		String value = null;
		List<Person> persons = getContentDao().findPersonByDisplayName(member.getValue().toString(), getDomain().getId());
		if (persons.size()==1) {
			Person person = persons.get(0);
			if ("email".equals(attribute)) {
				value = person.getEmail();
			}
			else
			if ("phone".equals(attribute)) {
				value = person.getPhone();
			}
		}
		return value;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

}
