package com.novamens.solr.indexer.iql;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.indexer.iql.CalculatedPredicate;

public class SolrBetweenDatesPredicate extends SolrAbstractPredicate implements CalculatedPredicate {
		
	private Classifier classifier;
	private Attribute attribute;
	
	
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

	
	// Dos argumentos: “desde días anteriores” y “hasta días anteriores” que cuentan los días anteriores a la fecha evaluada. 
	// Con esos argumentos se evalúa que el día de la fecha (today) este en el rango de fechas definido por los argumentos. 
	// Si el argumento es único se considera el rango desde el argumento hasta la fecha evaluada (tantos dias antes de la fecha).
	public String getCode(String arguments) {
		
		DateTimeFormatter formatter = getAttribute()!=null ? DateTimeFormatter.ISO_DATE_TIME : DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		String code = "";
		
		// Before:
		// [today+fromdays, today+dateto]
		OffsetDateTime datefrom = getFromDate(arguments); 
		OffsetDateTime dateto = getToDate(arguments); 
		code = getPath() +":["+ formatter.format(dateto) + " TO " + formatter.format(datefrom) + "]";
 		
		return code;
	}
	
	public boolean evaluate(Object object, Object arguments) {
		
		if (!(object instanceof Content)) 
			return false;

		OffsetDateTime datefrom = getFromDate((String)arguments); 
		OffsetDateTime dateto = getToDate((String)arguments); 
		
		Content content = (Content)object;
		
		for (String value : content.getAttributeValues(getAttribute())) {
			OffsetDateTime date = getDateValue(value);
			if (datefrom.isBefore(date) && dateto.isAfter(date)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
		setPath(classifier.getUniqueName()+"member");
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
	
	private OffsetDateTime getFromDate(String arguments) {
		try {
			if (arguments==null || arguments.contains(","))
				throw new IllegalArgumentException(arguments);
			String values[] = arguments.split(",");
			if (values.length!=2)
				throw new IllegalArgumentException(arguments);
			OffsetDateTime from = getDateArgument(values[0].trim());
			return from;
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(arguments);
		}
	}
	
	private OffsetDateTime getToDate(String arguments) {
		try {
			if (arguments==null || arguments.contains(","))
				throw new IllegalArgumentException(arguments);
			String values[] = arguments.split(",");
			if (values.length!=2)
				throw new IllegalArgumentException(arguments);
			OffsetDateTime to = getDateArgument(values[1].trim());
			return to;
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(arguments);
		}
	}
	

	private OffsetDateTime getDateValue(String value) {
		LocalDateTime localDate =  LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
		ZoneOffset offset = ZoneOffset.UTC;
		OffsetDateTime offsetDateTime = OffsetDateTime.of(localDate, offset);
		return offsetDateTime;
	}
	
	private OffsetDateTime getDateArgument(String argument) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDateTime localDate = LocalDateTime.parse(argument, formatter);
			ZoneOffset offset = ZoneOffset.UTC;
			OffsetDateTime offsetDateTime = OffsetDateTime.of(localDate, offset);
			return offsetDateTime;
		}
		catch (DateTimeParseException e) {
			throw new IllegalArgumentException(argument);
		}
	}

}
