package com.novamens.solr.indexer.iql;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;

import com.novamens.indexer.iql.CalculatedPredicate;

public class SolrDatePredicate extends SolrAbstractPredicate implements CalculatedPredicate {

	static private final DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
	private Classifier classifier;
	private Attribute attribute;
	private boolean isAfter = false;
	private long amountdays = 0; 
	
	public boolean isCanonical() {
		return false;
	}
	
	public boolean isInformationModel() {
		return true;
	}
	
	
	@Override
	public String getHelpValueTypeDescription() {
		return 	"date";
	}
	
	public String getCode(String argument) {
		
		OffsetDateTime date = null;
		DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
		
		if ("today".equals(argument.toLowerCase())) {
			date = OffsetDateTime.now();
			argument = formatter.format(date);
		}
		else {
			date = parse(argument);
		}
		
		String code = "";
		
		if (!isAfter()) {
			// Before:
			if (amountdays>0) {
				// [date, date+amount]
				OffsetDateTime datelimit = date.plus(amountdays, ChronoUnit.DAYS); 
				String limit = formatter.format(datelimit);
				code = getPath() +":["+ argument + " TO " + limit + "]";
			}
			else {
				// [*, date]
				code = getPath() +":[ * TO " + argument + "]";
			}
		}
		else {
			
		}
 		
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
		
		for (Classification classification : content.getClassification()) {
			if (classification!=null && classification.getClassifier()!=null && classification.getClassifier().equals(getClassifier())) {
				if (classification.getDateValue()!=null) {
					if (classification.getDateValue().truncatedTo(ChronoUnit.DAYS).isBefore(dateargument.truncatedTo(ChronoUnit.DAYS)))
						return true;
				}	
			}
		}
		
		return false;
	}
	
	public void setAfter(boolean value) {
		isAfter = value;
	}
	
	public boolean isAfter() {
		return isAfter;
	}
	
	public boolean isBefore() {
		return !isAfter;
	}
	
	public void setBefore(boolean value) {
		isAfter = !value;
	}
	
	public void setBeforeDays(long value) {
		amountdays = value;
		isAfter = false;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
		setName(classifier.getName());
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
		setPath(attribute.getUniqueName()+"name");
		setName(attribute.getName());
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	private OffsetDateTime parse(String argument) {
		try {
			OffsetDateTime date= OffsetDateTime.parse(argument, dateformat);
			return date;
		}
		catch (Exception e) {
			throw e;
		}
	}
}
