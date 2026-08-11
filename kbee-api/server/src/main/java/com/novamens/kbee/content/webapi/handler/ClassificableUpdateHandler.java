package com.novamens.kbee.content.webapi.handler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.AttributeType;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DataSetService;
import com.novamens.kbee.content.repository.MemberRepository;
import com.novamens.kbee.lock.LockTransactionSynchronization;
import com.novamens.repository.DomRepository;

import kbee.api.model.ApiValue;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ApiClassificable;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.logging.Logger;

public abstract class ClassificableUpdateHandler extends AbstractRequestHandler {
	
	static private Logger logger = Logger.getLogger(ClassificableUpdateHandler.class.getName());
	
	@Autowired
	private DomRepository<DataSetMember> memberRepository;
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> setAttributes(Classificable classificable, ApiClassificable iclassificable) {
		List<String> updates = new ArrayList<String>();
		Set<String> names = new HashSet<String>();
		for (IAttributeValues attributevalue : iclassificable.getAttributes()) {
			ApiAttributeProxy iattribute = attributevalue.getAttribute();
			String attributename = iattribute.getName().toLowerCase(); 
			if (!names.contains(attributename) && !getValues(attributename, iclassificable).isEmpty()) {
				Classifier classifier = getClassifier(attributename, classificable);
				if (classifier!=null) {
					names.add(attributename);
					if (update(classificable, classifier, getValues(attributename, iclassificable))) {
						classifiy(classificable, classifier, getValues(attributename, iclassificable));
						updates.add(iattribute.getName());
					}
				}
				else {
					Attribute attribute = getAttribute(attributename, classificable);
					if (attribute!=null) {
						names.add(attributename);
						if (update(classificable, attribute, getValues(attributename, iclassificable))) {
							setAttribute(classificable, attribute, getValues(attributename, iclassificable));
							updates.add(iattribute.getName());
						}
					}
					else {
						throw new ApiException(HttpStatus.PRECONDITION_FAILED,  ApiError.INVALID_ATTRIBUTE, iattribute.getName());
					}
				}
			}
		}
		for (Classifier classifier : getClassifiers(classificable)) {
			if (!names.contains(classifier.getName().toLowerCase()) && !names.contains(classifier.getAlias().toLowerCase())) {
				if (isMandatory(classifier, classificable)) {
					throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.ATTRIBUTE_IS_REQUIRED, classifier.getName());
				}
				else {
					if (update(classificable, classifier, new ArrayList<ApiValue>())) {
						updates.add(classifier.getName());
						classificable.setClassification(classifier, new ArrayList<DataSetMember>());
					}
				}
			}
		}
		for (AttributeTemplate template : getAttributes(classificable)) {
			if (!names.contains(template.getAttribute().getName().toLowerCase())) {
				if (isMandatory(template.getAttribute(), classificable)) {
					throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.ATTRIBUTE_IS_REQUIRED, template.getAttribute().getName());
				}
				else {
					if (update(classificable, template.getAttribute(), new ArrayList<ApiValue>())) {
						classificable.setAttributeValues(template.getAttribute(), new ArrayList<String>());
					}
				}
			}
		}
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Classifier getClassifier(String classifiername, Classificable classificable) {
		for (Classifier classifier : getClassifiers(classificable)) {
			if (classifiername.equals(classifier.getName().toLowerCase()) ||
					(classifier.getAlias() != null && classifiername.equals(classifier.getAlias().toLowerCase()))) {
				return classifier;
			}
		}
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected abstract List<Classifier> getClassifiers(Classificable member);
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean isMandatory(Classifier classifier, Classificable member) {
		if (member instanceof Content) {
			for (ClassifierTemplate template : ((Content)member).getContentTemplate().getClassifiers()) {
				if (classifier.equals(template.getClassifier())) {
					return template.isMandatory();
				}
			};
		}	
		return classifier.isMandatory();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean  isMandatory(Attribute attribute, Classificable member) {
		if (member instanceof Content) {
			for (AttributeTemplate template : ((Content)member).getContentTemplate().getAttributes()) {
				if (attribute.equals(template.getAttribute())) {
					return template.isMandatory();
				}
			};
		}	
		return attribute.isRequired();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Attribute getAttribute(String attributename, Classificable member) {
		for (AttributeTemplate template : getAttributes(member)) {
			if (attributename.equals(template.getAttribute().getName().toLowerCase()) ||
					(template.getAttribute().getAlias() != null && attributename.equals(template.getAttribute().getAlias().toLowerCase()))) {
				return template.getAttribute();
			}
		}
		return null;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected abstract List<AttributeTemplate> getAttributes(Classificable member);
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<ApiValue> getValues(String attributename, ApiClassificable file) {
		List<ApiValue> values = new ArrayList<ApiValue>();
		for (IAttributeValues attributevalues : file.getAttributes()) {
			if (attributename.equals(attributevalues.getAttribute().getName().toLowerCase())) {
				for (ApiValue value : attributevalues.getValues()) {
					if (value.getHRef()!=null) {
						value = getValue(value.getHRef()); 
						values.add(value);
					}
					else
					if (value.getValue()!=null) {
						values.add(value);
					}
				}
			}
		}
		return values;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean update(Classificable content, Classifier classifier, List<ApiValue> values) {
		List<DataSetMember> members = new ArrayList<DataSetMember>();

		if(classifier.getDataSetType().equals(DataSetType.DATE))
			return true;

		for (Classification classification : content.getClassification()) {
			if (classification.getClassifier().equals(classifier)) {
				members.add(classification.getDataSetMember());
			}
		}
		if (members.size()!=values.size())
			return true;
		for (ApiValue value : values) {
			boolean classified = false;
			for (DataSetMember member : members) {
				if (value.getId()!=null) {
					if (String.valueOf(member.getId()).equals(value.getId())) {
						classified = true;
						break;
					}
				}
				else {
					if (member.getDisplayName().equals(value.getDisplayName())) {
						classified = true;
						break;
					}
				}
			}
			if (!classified && value!=null)
				return true;
		}
		return false;
		
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected void classifiy(Classificable content, Classifier classifier, List<ApiValue> values) {
		if ((classifier.getMultiplicity().equals(Multiplicity.M01)|| classifier.getMultiplicity().equals(Multiplicity.M11)) && values.size()>1) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_MULTIPLICITY, classifier.getName());
		}
		if (classifier.getDataSetType().equals(DataSetType.DATE)) {
			List<OffsetDateTime> dates = new ArrayList<OffsetDateTime>();
			for (ApiValue value : values) {
				DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime datetime = LocalDateTime.parse(value.getValue() + " 00:00:00", dateformat);
				OffsetDateTime localvalue = OffsetDateTime.of(datetime, OffsetDateTime.now().getOffset());
				dates.add(localvalue);
			}
			((Content)content).setValues(classifier, dates);
		}
		else {
			List<DataSetMember> members = new ArrayList<DataSetMember>();
			if (classifier.getDataSet().isAggregation()) {
				DataSetMember aggregator = getAggregator(content, classifier);
				if (aggregator==null) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.AGGREGATOR_NOT_FOUND, classifier.getName());
				}
				for (ApiValue value : values) {
					DataSetMember member;
					if (value.getId()!=null) {
						member = findMemberById(value.getId());
					}	 
					else {
						member = findAggregationByValue(aggregator, classifier.getDataSet(), value.getValue());
					}
					members.add(member);
				}
			}
			else {
				for (ApiValue value : values) {
					DataSetMember member;
					if (value.getId()!=null) {
						member = findMemberById(value.getId());
					}	
					else {
						member = findMemberByValue(classifier.getDataSet(), value.getValue());
					}
					members.add(member);
				}
			}
			content.setClassification(classifier, members);
		}	
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected DataSetMember findMemberByValue(DataSet dataSet, String value) {
		try {
			DataSetMember member = getContentDao().findMemberByValue(dataSet, value);
			if (member==null) {
				new LockTransactionSynchronization("ds"+String.valueOf(dataSet.getId()));
				member = getContentDao().findMemberByValue(dataSet, value);
				if (member==null) {
					member = dataSet.createMember();
					member.setStrValue(value);
					member.getService(DOMObjectService.class).update();
				}
			}
			return member;
		}
		catch(ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	protected DataSetMember findAggregationByValue(DataSetMember aggregator, DataSet aggregation, String value) {
		try {
			DataSetMember member = getMemberRepository().findAggregationByValue(aggregator, aggregation, value);
			if (member==null) {
				new LockTransactionSynchronization("ds"+String.valueOf(aggregator.getId()));
				member = getMemberRepository().findAggregationByValue(aggregator, aggregation, value);
				if (member==null) {
					member = aggregation.createMember();
					member.setStrValue(value);
					Classifier classifier = aggregation.getService(DataSetService.class).getAggregatorClassifier();
					List<DataSetMember> members = new ArrayList<DataSetMember>();
					members.add(aggregator);
					member.setClassification(classifier, members);
					member.getService(DOMObjectService.class).update();
				}
			}
			return member;
		}
		catch(ContentMgmtException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected DataSetMember findMemberById(String value) {
		try {
			DataSetMember member = getContentDao().findMemberById(Long.valueOf(value));
			if (member==null || !member.getDomain().equals(getDomain())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.VALUE_NOT_FOUND);
			}
			return member;
		}
		catch(ContentMgmtException | NumberFormatException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	protected DataSetMember getAggregator(Classificable content, Classifier aggregationclassifier) {
		Classifier aggregatorClassifier =  aggregationclassifier.getDataSet().getService(DataSetService.class).getAggregatorClassifier();
		if (aggregatorClassifier==null) 
			return null;
		List<Classification> aggregatorClassification = content.getClassification(aggregatorClassifier);
		DataSetMember aggregator = aggregatorClassification.size()==1 ? 
			aggregatorClassification.get(0).getDataSetMember() : 
			null; 
		return aggregator; 
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected boolean update(Classificable content, Attribute attribute, List<ApiValue> values) {
		for (ApiValue value1 : values) {
			boolean found = false;
			for (String value2 : content.getAttributeValues(attribute)) {
				if (value1.getValue().equals(value2)) {
					found = true;
					break;
				}
				else {
					if (attribute.getType().equals(AttributeType.DATE)) {
						try {
							if (formatDate(value1.getValue()).equals(value2)) {
								found = true;
								break;
							}
						}
						catch (DateTimeParseException e) {
						}
					}
				}
			}
			if (!found && !"".equals(value1.getValue().trim())) {
				return true;
			}
		}
		return false;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiValue getValue(String url) {
		String fragments[] = url.split("/");
		int n = fragments.length;
		if (n>1 && "externalvalue".equals(fragments[n-1])) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.VALUE_NOT_FOUND);
		}
		else {
			String id = fragments[n-1];
			ApiValue value = new ApiValue();
			value.setId(id);
			return value;
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected MemberRepository getMemberRepository() {
		return (MemberRepository)memberRepository;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private void setAttribute(Classificable content, Attribute attribute, List<ApiValue> values) {
		if ((attribute.getMultiplicity().equals(Multiplicity.M01)|| attribute.getMultiplicity().equals(Multiplicity.M11)) && values.size()>1) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_MULTIPLICITY, attribute.getName());
		}
		if (attribute.getType().equals(AttributeType.DATE)) {
			List<String> datevalues = new ArrayList<String>();
			for (ApiValue value : values) {
				try {
					datevalues.add(formatDate(value.getValue()));
				}
				catch (DateTimeParseException e) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_DATE, attribute.getName());
				}
			}
			content.setAttributeValues(attribute, datevalues);
		}
		else {
			List<String> stringvalues = new ArrayList<String>();
			for (ApiValue value : values) {
				stringvalues.add(value.getValue());
			}
			content.setAttributeValues(attribute, stringvalues);
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	private String formatDate(String value) throws DateTimeParseException {
		DateTimeFormatter inputdateformat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime localdatetime = LocalDateTime.parse(value + " 00:00:00", inputdateformat);
		OffsetDateTime date = OffsetDateTime.of(localdatetime, OffsetDateTime.now().getOffset());
		DateTimeFormatter dateformat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		String datevalue = dateformat.format(date);
		return datevalue;
	}
}
