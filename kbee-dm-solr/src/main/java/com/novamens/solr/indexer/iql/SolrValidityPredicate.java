package com.novamens.solr.indexer.iql;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.service.ContentService;
import com.novamens.indexer.iql.CalculatedPredicate;
			
public class SolrValidityPredicate extends SolrAbstractPredicate implements CalculatedPredicate {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrValidityPredicate.class.getName());
	
	static private final DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
	private Attribute initialvalidity, finalvalidity;
	
	public SolrValidityPredicate(Attribute initalvalidity, Attribute finalvalidity) {
		this.initialvalidity = initalvalidity;
		this.finalvalidity = finalvalidity;
	}
	
	
	public boolean isTimed() {
		return true;
	}
	
	public boolean isInformationModel() {
		return false;
	}

	
	public boolean isCanonical() {
		return false;
	}
	
	
	@Override
	public String getHelpValueTypeDescription() {
		return 	"from, to";
	}
	
	public String getCode(String argument) {
		
		if ("today".equals(argument.toLowerCase())) {
			argument = dateformat.format(OffsetDateTime.now());
		}
		
		String code = "(";
		code += initialvalidity.getUniqueName()+"member:";
		code += "[ * TO " + argument + "]";
		code += " AND ";
		code += finalvalidity.getUniqueName()+"member:";
		code += "[" + argument + " TO * ]";
		code += ")";
		
		return code;
	}
	
	
	
	public boolean evaluate(Object object, Object argument) {
		
		if (!(object instanceof Content)) 
			return false;

		OffsetDateTime dateargument;
		
		if (argument!=null && "today".equals(argument.toString().toLowerCase())) {
			dateargument = OffsetDateTime.now();
		}
		else {
			dateargument = parse((String)argument);
			if (dateargument==null) { 
				return false;
			}
		}
		
		Content content = (Content)object;
		
		boolean valid = content.getService(ContentService.class).getValidVersion()!=null;
		
//		List<String> values = content.getAttributeValues(initialvalidity);
//		if (values!=null && values.size()==1) {
//			String value = values.get(0);
//			OffsetDateTime initial = null;
//			try {
//				initial = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//			}
//			catch (Exception e) {
//				logger.error(e);
//			}
//			if (initial==null || initial.isAfter(dateargument)) {
//				return false;
//			}
//		}
//		
//		values = content.getAttributeValues(finalvalidity);
//		if (values!=null && values.size()==1) {
//			String value = values.get(0);
//			value = value.replace("00:00:00", "23:59:59");
//			OffsetDateTime finalvalidity = null;
//			try {
//				finalvalidity = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
//			}
//			catch (Exception e) {
//			}
//			if (finalvalidity==null || finalvalidity.isBefore(dateargument)) {
//				return false;
//			}
//		}
		
		return valid;
	}
	
	private OffsetDateTime parse(String argument) {
		try {
			OffsetDateTime date = OffsetDateTime.parse(argument, dateformat);
			return date;
		}
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
}
