package com.novamens.kbee.content.text.template;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.service.UrlService;
import com.novamens.content.text.template.Variable;
import com.novamens.content.text.template.VariableResolver;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

public class ContentVariableResolver implements VariableResolver {
																
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentVariableResolver.class.getName());
	
	private Content content;
	
	
	
	public ContentVariableResolver(Content content) {
		this.content = content;
	}
	
	
	public String getValue(Variable variable) {
		
		try {
			if (variable.getType().equals("classifier"))				return getClassification(variable);
			if (variable.getType().equals("attribute")) 				return getAttribute(variable);
			if (variable.getType().equals("file")) 						return getFileAttribute(variable);
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
		
		if (variable.getFormat()!=null && attribute!=null && attribute.getType().equals(AttributeType.DATE)) {
			try {
			
				
				logger.debug(value + " | format: " + variable.getFormat()+" | " );
						
				// '2011-12-03T10:15:30'
				
				LocalDateTime date =  LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
				DateTimeFormatter formatter= DateTimeFormatter.ofPattern(variable.getFormat());
				
				logger.debug(value + " | format: " + variable.getFormat()+ " | Date ->  " + date.toString() + " |  result -> " + formatter.format(date));
				
				value = formatter.format(date);
				
				
			}
			catch (Exception e) {
				logger.error(e);
				return null;
			}
		}
		
		if (value!=null && variable.getFormat()!=null && "capital".equals(variable.getFormat().toLowerCase())) {
			if (value.length()>1) {
				value = value.substring(0,1).toUpperCase() + value.substring(1).toLowerCase();
			}
			else {
				value = value.toUpperCase();
			}
		}
		return value;
	}
	
	private String getFileAttribute(Variable variable) {
		if ("id".equals(variable.getName()))
			return getContent().getId().toString();
		else
		if ("oid".equals(variable.getName()))
			return getContent().getOId().toString();
		if ("version".equals(variable.getName()))
			return String.valueOf(getContent().getVersion());
		else
		if ("library-url".equals(variable.getName()))
			return getServerUrl(getContent().getDomain())  + "/" +  getContent().getClassCode()	+ "/" +  String.valueOf(getContent().getOId());
		else
		if ("first-resource-title".equals(variable.getName())) {
			return getContent() instanceof ResourceContainer && !((ResourceContainer)getContent()).getResources().isEmpty() ?
				((ResourceContainer)getContent()).getResources().get(0).getTitle() :
				"-";	
		}	
		return null;
	}
	
	
	public Content getContent() {
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
		for (ModelElementTemplate template : member.getDataSet().getStructure()) {
			if (template!=null && template.getElement().getName().toLowerCase().equals(attribute.toLowerCase()) &&
					template.getElement() instanceof Attribute) {
				List<String> values = member.getAttributeValues((Attribute)template.getElement());
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
	
	private String getServerUrl(Domain domain) {
		return domain.getService(UrlService.class).getServerUrl();
		
		//return vanity_server.trim().replace("${domain}", domain.getName()) + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
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
