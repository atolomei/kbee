 package com.novamens.solr.indexer.iql;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.novamens.content.base.Content;
import com.novamens.indexer.iql.CalculatedPredicate;

public class SolrAfterPublishPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	
	public SolrAfterPublishPredicate() {
		setPath("published");
		setName("published_afterdays");
	}
	
	
	public boolean isInformationModel() {
		return false;
	}
	
	@Override
	public boolean isTimed() {
		return true;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	@Override
	public String getHelpValueTypeDescription() {
		return 	"from, to";
	}
	
	public String getCode(String arguments) {
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
		
		int fromdays = getFromDays(arguments);
		int todays	= getToDays(arguments);	
		
		OffsetDateTime today = OffsetDateTime.now();
		
		String code = "";
		
		OffsetDateTime datefrom = today.minus(todays, ChronoUnit.DAYS); 
		OffsetDateTime dateto = today.minus(fromdays, ChronoUnit.DAYS); 
		code = getPath() +":["+ formatter.format(datefrom) + " TO " + formatter.format(dateto) + "]";
 		
		return code;
	}
	
	public boolean evaluate(Object object, Object arguments) {
		
		if (!(object instanceof Content)) 
			return false;
		
		int fromdays = getFromDays((String)arguments);
		int todays	= getToDays((String)arguments);
		
		Content content = (Content)object;
		
		OffsetDateTime today = OffsetDateTime.now();
		OffsetDateTime date = content.getCheckinOffsetDateTime();
		OffsetDateTime datefrom = date.plus(fromdays, ChronoUnit.DAYS); 
		OffsetDateTime dateto = date.plus(todays, ChronoUnit.DAYS); 
		
		if (today.isAfter(datefrom) && today.isBefore(dateto)) {
			return true;
		}
		
		return false;
	}
	

	
	private int getFromDays(String arguments) {
		try {
			if (arguments==null)
				throw new IllegalArgumentException(arguments);
			if (!arguments.contains(",")) {
				Integer.valueOf(arguments);
				return 0;
			}
			else {
				String values[] = arguments.split(",");
				if (values.length!=2)
					throw new IllegalArgumentException(arguments);
				Integer from = Integer.valueOf(values[0].trim());
				return from;
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(arguments);
		}
	}
	
	private int getToDays(String arguments) {
		try {
			if (arguments==null)
				throw new IllegalArgumentException(getName() + " null");
			if (!arguments.contains(",")) {
				Integer value = Integer.valueOf(arguments);
				return value;
			}
			else {
				String values[] = arguments.split(",");
				if (values.length!=2)
					throw new IllegalArgumentException(getName() + " "+arguments);
				Integer from = Integer.valueOf(values[0].trim());
				Integer to = Integer.valueOf(values[1].trim());
				if (to<from)
					throw new IllegalArgumentException(getName() + " "+arguments);
				return to;
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(getName() + " "+arguments);
		}
	}
}
