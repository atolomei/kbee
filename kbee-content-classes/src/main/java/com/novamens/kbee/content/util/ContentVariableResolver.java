package com.novamens.kbee.content.util;


// TODO VER ESTO
//

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.service.UrlService;
import com.novamens.content.text.template.Variable;
import com.novamens.content.text.template.VariableResolver;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.text.template.TemplateData;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;


/**
 * 
 *  "MMM dd yyyy"
 *  "dd MMM yyyy"
 * 
 *
 * @param <T>
 */
public class ContentVariableResolver<T extends Content> implements VariableResolver {
	
	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(ContentVariableResolver.class.getName()));
	
	private IModel<T> contentModel;
	private IModel<TemplateData> dataModel;
	
	
	public ContentVariableResolver(IModel<T> contentModel, IModel<TemplateData> dataModel) {
		setContentModel(contentModel);
		setDataModel(dataModel);
	}
	
	/**
	 * 
	 *   
	 *  workflow-procedure
	 *  workflow-date-started
	 *  file-version
	 *  
	 *  
	 * 
	 * 
	 *  $file-library-url$ 		-> file url in Libray
	 *  $file-Oid$ 				-> file oid
	 *  $library-url$ 			-> library url in general
	 *    
	 *   classifier
	 *   attribute
	 *  
	 *  field:date -> editable
	 *  field:string -> editable
	 *  field:number -> editable
	 * 
			$include:Header$
			Date. $field:Date:date:MMMMMM dd, YYYY$
			To. $classifier:Property$
			Re. $attribute:Household Last Name$, $attribute:Household First Name$
			Unit Type. $field:Unit Type:string$
			Unit Number. $attribute:Unit$
			Effective Date. $classifier:Effective Date:MMM dd, YYYY$ 
			Approval Expiration. $field:Approval Expiration:date:MMMMM dd, YYYY$
			
			This certification has received final-approval for the household, certification effective date and unit type noted above. 
			This file is now in the Compliance Library.
			Thank you, 
			RealPage Compliance Services
			$include:Footer$

	 */
	public String getValue(Variable variable) {
		
		if (variable.getType().equals("classifier")) {
			return getClassification(variable);
		}
		if (variable.getType().equals("attribute")) {
			String value = getClassification(variable);
			if (value==null || getDefaultValue().equals(value)) 
				value = getAttribute(variable);
			return value;
		}
		if (variable.getType().equals("field")) {
			String value = getData().get(variable.getName());
			if (value!=null)
			value = value.replace("\\'", "\"");
			if (variable.getFormat()!=null) {
				value = format(variable, value);
			}
			return value;
		}
		if (variable.getName().equals("date")) {
			return getDate(variable);
		}
		if (variable.getType().equals("user")) {
			return getUserAttribute(variable);
		}
		if (variable.getName().equals("time")) {
			return getTime();
		}
		if (variable.getType().equals("file")) {
			return getFileAttribute(variable);
		}
		
		if (variable.getType().equals("library")) {
			if ("url".equals(variable.getName()))
				return getServerUrl(getContentModel().getObject().getDomain()) + "/content";
		}

		return getDefaultValue();
	}
	
	
	private String getServerUrl(Domain domain) {
		return domain.getService(UrlService.class).getServerUrl();
		//return vanity_server.trim().replace("${domain}", domain.getName()) + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
	}
	
	
	public String getClassification(Variable variable) {

		String classifiername, attributename, value = "-";
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
					
					logger.error(Thread.currentThread().getStackTrace()[1].getMethodName()+ " | Variable " +  variable.getName() + " does have a Date Formatter. Using System Default ");
					DateTimeFormatter fo = DateTimeService.getDefaultDateTime_Date_Formatter();
					OffsetDateTime od = OffsetDateTime.ofInstant(((Date)object).toInstant(), ZoneId.systemDefault());
					return fo.format(od);
				}	
			}
			catch (Exception e) {
				logger.error(e);
				return getDefaultValue();
			}
		}
		
		
		if (object instanceof OffsetDateTime) {
			try {
				if (variable.getFormat()!=null) {
					String s = variable.getFormat();
					//s=s.replace("MMMM", "MMM");
					DateTimeFormatter format = DateTimeFormatter.ofPattern(s, getSessionUser().getLocale());
					//DateTimeFormatter format = DateTimeFormatter.ISO_DATE;
					value = format.format((OffsetDateTime)object);
					return value;
				}
				else {
					
					logger.error(Thread.currentThread().getStackTrace()[1].getMethodName()+ " | Variable " +  variable.getName() + " does have a OffsetDateTime DateTimeFormatter.  Using System Default ");
					DateTimeFormatter fo = DateTimeService.getDefaultDateTime_Date_Formatter();
					return fo.format((OffsetDateTime)object);
				}	
			}
			catch (Exception e) {
				logger.error(e);
				return getDefaultValue();
			}
		}
		
		DataSetMember member = null;
		
		if (object instanceof DataSetMember) {
			member = (DataSetMember)object;
		}
		if (member==null) {
			return getDefaultValue();
		}
		attributename = variable.getAttribute();
		if (attributename!=null) {
			value = getAttribute(member, attributename);
		}
		else {
			value = member.getDisplayName();
		}
		if (value==null) {
			return getDefaultValue();
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
			List<String> values = getContent().getAttributeValues(attribute);
			if (!values.isEmpty()) {
				value = values.get(0);
			}
		}
		return value;
	}
	
	public void setContentModel(IModel<T> model) {
		this.contentModel = model;
	}
	
	public IModel<T> getContentModel() {
		return this.contentModel;
	}
	
	public void setDataModel(IModel<TemplateData> model) {
		this.dataModel = model;
	}
	
	public IModel<TemplateData> getDataModel() {
		return this.dataModel;
	}
	
	public TemplateData getData() {
		return getDataModel().getObject();
	}
	
	public T getContent() {
		return getContentModel().getObject();
	}
	
	public String getDate(Variable variable) {
		String format = variable.getFormat();
		if (format==null) {
			format="MMM dd YYYY";
		}
		else
			format=format.replace("MMMM", "MM");

		String formated;
		
		try {
			formated = DateTimeFormatter.ofPattern(format).format(OffsetDateTime.now());
		} catch (Exception e) {
			formated = DateTimeFormatter.ISO_DATE.format(OffsetDateTime.now());
		}
		
		return formated;
		
	}
	
	
	/**
	 * 
	 * @return
	 */
	
	public String getTime() {
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		return service.getDateDisplayString(OffsetDateTime.now(), getSessionUser().getLocale());
	
	}
	
	
	
	public String format(Variable variable, String value) {
		if (value==null || variable.getFormat()==null || "".equals(variable.getFormat()))
			return value;
		
		if ("date".equals(variable.getValueType())) {
			try {

				DateTimeService service = ServiceLocator.getService(DateTimeService.class);
				OffsetDateTime odate=service.parseStrDate(value);
				return service.getDateDisplayString(odate);
				
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		return value;
	}
	
	protected String getDefaultValue() {
		return "-";
	}
	
	protected Object getClassification(String classifiername) {
		for (Classification classification : getContent().getClassification()) {
			if (classification!=null && classification.getClassifier().getName().toLowerCase().equals(classifiername.toLowerCase())) {
				if (classification.getDataSetType().equals(DataSetType.DATE))
					return classification.getDateValue();
				else	
					return classification.getDataSetMember();
			}
		}
		return null;
	}
	
	private String getAttribute(DataSetMember member, String attribute) {
		String value = null;
		for (AttributeTemplate template : member.getDataSet().getAttributes()) {
			if (template.getAttribute().getName().toLowerCase().equals(attribute.toLowerCase())) {
				List<String> values = member.getAttributeValues(template.getAttribute());
				if (!values.isEmpty()) {
					value = values.get(0);
					break;
				}
			}
		}
		if (value==null) {
			for (Classification classification : member.getClassification()) {
				if (classification!=null && ( classification.getClassifier().getName().toLowerCase().equals(attribute.toLowerCase()))) {
					if (classification.getDataSetType().equals(DataSetType.DATE))
						value = classification.getDateValue().toString();
					else	
						value = classification.getDataSetMember().getDisplayName();
					break;
				}
			}
		}
		return value;
	}
	
	private String getFileAttribute(Variable variable) {
		if ("id".equals(variable.getName()))
			return getContentModel().getObject().getId().toString();
		else
		if ("oid".equals(variable.getName()))
			return getContentModel().getObject().getOId().toString();
		if ("version".equals(variable.getName()))
			return String.valueOf(getContentModel().getObject().getVersion());
		else
		if ("library-url".equals(variable.getName()))
			return getServerUrl(getContentModel().getObject().getDomain())  + "/" +  getContentModel().getObject().getClassCode()	+ "/" +  String.valueOf(getContentModel().getObject().getOId());
		return null;
	}
	
	private String getUserAttribute(Variable variable) {
		String value = null;
		
		Person person = null; 
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		if (userProfile!=null) {
			person = (Person)userProfile.getEntity();
		}	

		try {
			if (userProfile!=null) {
				person = (Person)userProfile.getEntity();
				IModel<Object> model = new PropertyModel<Object>(person, variable.getName());
				Object object = model.getObject();
				if (object!=null) {
					value = object.toString(); 
				}		
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
		
		if (value!=null) {
			return value;
		}
		
		List<DataSetMember> members = getContentDao().findMembersByEntity(person);
		
		if (members.size()!=1) {
			return "-";
		}
		
		DataSetMember usermember = members.get(0);
		
		for (Classification classification : usermember.getClassification()) {
			if (classification!=null && (variable.getName().equals(classification.getClassifier().getAlias()) || classification.getClassifier().getName().toLowerCase().equals(variable.getName().toLowerCase()))) {
				value = classification.getDataSetMember().getDisplayName();
				break;
			}
		}
		
		return value;
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
