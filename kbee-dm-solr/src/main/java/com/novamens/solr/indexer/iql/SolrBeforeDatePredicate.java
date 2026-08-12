package com.novamens.solr.indexer.iql;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.indexer.iql.DateTimePredicate;
import com.novamens.service.ServiceLocator;

public class SolrBeforeDatePredicate extends SolrAbstractPredicate implements CalculatedPredicate, DateTimePredicate {
		
	private Classifier classifier;
	private Attribute attribute;
	
	
	public boolean isInformationModel() {
		return false;
	}
	
	@Override
	public boolean isCanonical() {
		return false;
	}
	
	@Override
	public boolean isTimed() {
		return true;
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
		
		int fromdays = getFromDays(arguments);
		int todays	= getToDays(arguments);	
		
		OffsetDateTime today = OffsetDateTime.now();
 		
		String code = "";
		
		// Before:
		// [today+fromdays, today+dateto]
		OffsetDateTime datefrom = today.plus(fromdays, ChronoUnit.DAYS); 
		OffsetDateTime dateto = today.plus(todays, ChronoUnit.DAYS); 
		
		
		String datefromstr = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(datefrom) + "T00:00:00";
		String datetostr = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dateto) + "T23:59:59";
		
		if (getClassifier()!=null) {
			datefromstr = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(datefrom);
			datetostr = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(dateto);
		}
		
//		code = getPath() +":["+ formatter.format(datefrom) + " TO " + formatter.format(dateto) + "]";
		code = getPath() +":["+ datefromstr + " TO " + datetostr + "]";
		
		return code;
	}
	
	public boolean evaluate(Object object, Object arguments) {
		
		if (!(object instanceof Content)) 
			return false;

		
		int fromdays = getFromDays((String)arguments);
		int todays	= getToDays((String)arguments);
		
		OffsetDateTime today = OffsetDateTime.now();
		OffsetDateTime datefrom = today.plus(fromdays, ChronoUnit.DAYS); 
		OffsetDateTime dateto = today.plus(todays, ChronoUnit.DAYS); 
		
		Content content = (Content)object;
		
		for (Classification classification : content.getClassification()) {
			if (classification!=null && classification.getClassifier()!=null && classification.getClassifier().equals(getClassifier())) {
				if (classification.getDateValue()!=null) {
					OffsetDateTime date = classification.getDateValue();
					if (datefrom.isBefore(date) && dateto.isAfter(date)) {
						return true;
					}
					else {
						return false;
					}
				}	
			}
		}
		
		return false;
	}
	
	public OffsetDateTime calculateDateTime (Object object, Object arguments) {
		if (!(object instanceof Content)) 
			return null;

		int fromdays = getFromDays((String)arguments);
	//	int todays	= getToDays((String)arguments);
		
		Content content = (Content)object;
		
		OffsetDateTime date = getDateTime(content);
		OffsetDateTime datefrom = date!=null ? date.minus(fromdays, ChronoUnit.DAYS) : null; 
		
		return datefrom;
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
	
	private OffsetDateTime getDateTime(Content content) {
		OffsetDateTime value = null;
		List<String> values = content.getAttributeValues(getAttribute());
		if (values.size()==1) {
			String stringvalue = values.get(0);
			try {
				value = OffsetDateTime.parse(stringvalue, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			}
			catch(Exception e) {
			}
		}
		return value;
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
