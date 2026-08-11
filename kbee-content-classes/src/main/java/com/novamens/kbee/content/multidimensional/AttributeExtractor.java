package com.novamens.kbee.content.multidimensional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.userlist.UserListItem;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Versionable;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.service.IndexerException;
import com.novamens.service.ServiceLocator;

public class AttributeExtractor implements Extractor {
	private Attribute attribute;
	
	static private Logger logger = LogManager.getLogger(AttributeExtractor.class.getName());
	
	public AttributeExtractor() {
	}
	
	public AttributeExtractor(Attribute attribute) {
		setAttribute(attribute);
	}
	
	public Object extract(Object object) throws IndexerException  {
		if (object instanceof UserListItem) {
			if (((UserListItem)object).getContent()!=null) {
				return extract(((UserListItem)object).getContent());
			}
		}
		List<String> values;
		if (getAttribute().getType().equals(AttributeType.DATE)) {
			values = new ArrayList<String>();
			List<String> t_v = getValues(object);
			if (t_v!=null) {
					for (String value :	t_v) {
						try {
							if (value!=null && value.endsWith("T00")) {
								value = value.substring(0, value.length()-3);
							}
							OffsetDateTime time = ServiceLocator.getService(DateTimeService.class).parseStrDate(value);
							String member = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(time);
							values.add(member);
						}
						catch (Exception e) {
							if (logger.isDebugEnabled()) {
								logger.error("parsing value -> "+value!=null?value:"null value", e);
							}
						}
					}
			}
		}
		else if (getAttribute().getType().equals(AttributeType.VALIDITY_TO) || getAttribute().getType().equals(AttributeType.VALIDITY_FROM)) {
			values = new ArrayList<String>();
			if (object instanceof Classificable) {
				values.add(getValidity((Classificable)object));
				if (logger.isDebugEnabled()) {
					logger.debug("validity "+getValidity((Classificable)object));
				}
			}
		}
		else {
			values = getValues(object);
		}
		
		return values;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	private List<String> getValues(Object object) {
		
		if (object instanceof Content) {
			return ((Content)object).getAttributeValues(getAttribute());
		}
		else {
			if (object instanceof DataSetMember) {
				return ((DataSetMember)object).getAttributeValues(getAttribute());
			}
			else {
				if (object instanceof Classificable) {
					return ((Classificable)object).getAttributeValues(getAttribute());
				}
				else {
					return new ArrayList<String>();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private String getValidity(Classificable content) {
		String validity = null;

		if (content instanceof Versionable && 
			getAttribute().getType().equals(AttributeType.VALIDITY_FROM)) {
			
			validity =  getVersionValidity(content);
	
			while (content!=null && validity!=null) {
				
				content = ((Versionable<Content>)content)
					.getPreviousVersion();

				String versionvalidity =  content!=null 
					? getVersionValidity(content)
					: validity;		
				
				if (versionvalidity==null || versionvalidity.compareTo(validity)<0) {
					validity = versionvalidity;
				}
			}
		}
		else {
			validity = getVersionValidity(content);
		}
		
		if (validity==null) {
			validity = getAttribute().getType().equals(AttributeType.VALIDITY_FROM)
				? "0000-00-00" 
				: "9999-99-99";
		}
		
		return validity;
	}
	
	private String getVersionValidity(Classificable content) {
		String versionvalidity = null;
		List<String> attributevalues = content.getAttributeValues(getAttribute());
		if (attributevalues.size()==1) {
			String value = attributevalues.get(0);
			try {
				TemporalAccessor time = ServiceLocator.getService(DateTimeService.class).parseStrDate(value);
				versionvalidity = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(time);
			}
			catch (Exception e) {
				logger.error(e);
			}
		}
		return versionvalidity;
	}
}
