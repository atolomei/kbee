package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.model.PersonMember;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.content.form.KbeeMemberForm;
import com.novamens.kbee.content.form.KbeeUserForm;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ApiAttributeProxy;
import kbee.api.model.IAttributeValues;
import kbee.api.model.IFieldData;
import kbee.api.model.IFormData;
import kbee.util.logging.Logger;

public class IValueAdapter implements Adapter<DataSetMember, ApiValue> {
	
	private static Logger logger = Logger.getLogger(IValueAdapter.class.getName());

	private String version;
	
	public IValueAdapter() {
	}
	
	public IValueAdapter(String version) {
		setVersion(version);
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public ApiValue adapt(DataSetMember member) {
		
		ApiValue value =  new ApiValue();
		
		value.setId(String.valueOf(member.getId()));
		value.setState(member.getState().name());
		value.setDisplayName(member.getDisplayName());
		value.setSubline(getSubline(member));
		value.setLastModifiedDate(member.getLastModifiedOffsetDateTime());
		value.setDomain(member.getDomain().getName());
		value.setDomainRef(new ApiProxy(String.valueOf(member.getDomain().getId()), 
				member.getDomain().getName(), 
				UriHelper.getUri(member.getDomain()), 
				"domain"));
		value.setDataSet(new ApiProxy(String.valueOf(member.getDataSet().getId()), member.getDataSet().getName(), UriHelper.getUri(member.getDataSet()), "dataset")); 
		value.setLastModifiedUser(new ApiUserProxy(member.getLastModifiedUser()));
		value.setExternalId(member.getExternalId());
		
		if (member instanceof PersonMember) {
			value.setFirstName(((PersonMember)member).getFirstName());
			value.setLastName(((PersonMember)member).getLastName());
			value.setEmail(((PersonMember)member).getEmail());
			Person person = ((PersonMember)member).getPerson();
			value.setPerson(new ApiProxy(String.valueOf(person.getId()), person.getDisplayName(), UriHelper.getUri(person), "person"));
		}
		
		List<IAttributeValues> values = new ArrayList<IAttributeValues>();
		
		for (Classification classification : member.getClassification()) {
			if (classification!=null && classification.getDataSetMember()!=null) {
				Classifier classifier = classification.getClassifier();
				ApiAttributeProxy attribute = new ApiAttributeProxy();
				attribute.setHRef(UriHelper.getUri(classifier));		
				attribute.setId(String.valueOf(classifier.getId()));
				attribute.setRel("classifier");
				attribute.setName(classifier.getName());
				DataSetMember classifiedmember = classification.getDataSetMember();
				ApiValue attributevalue = new ApiValue();
				attributevalue.setId(String.valueOf(classifiedmember.getId()));
				attributevalue.setValue(classifiedmember.getDisplayName());
				attributevalue.setDataSet(new ApiProxy(String.valueOf(classifiedmember.getDataSet().getId()), classifiedmember.getDataSet().getName(), UriHelper.getUri(classifiedmember.getDataSet()), "dataset")); 
				values.add(new IAttributeValues(attribute, attributevalue));
			}
		}
		
		for (AttributeTemplate template : member.getDataSet().getAttributes()) {
			List<String> attributevalues = member.getAttributeValues(template.getAttribute());
			if (attributevalues!=null && !attributevalues.isEmpty()) {
				for (String attributevalue : attributevalues) {
					ApiAttributeProxy iattribute = new ApiAttributeProxy();
					iattribute.setHRef(UriHelper.getUri(template.getAttribute()));		
					iattribute.setId(String.valueOf(template.getAttribute().getId()));
					iattribute.setRel("attribute");
					iattribute.setName(template.getAttribute().getName());
					ApiValue ivalue = new ApiValue();
					ivalue.setValue(attributevalue);
					values.add(new IAttributeValues(iattribute, ivalue));
				}
			}
		}

		value.setAttributes(values);
		
//		if (member.getParent()!=null) {
//			value.setParent(new IProxy(String.valueOf(member.getParent().getId()), member.getParent().getName(), UriHelper.getUri(member.getParent()), "member"));
//		}
		
		value.setFormData(getFormData(member));
		
		return value;	
	}
	
	private IFormData getFormData(DataSetMember member) {
		EForm form = getForm(member);
		IFormData idata = new IFormData();
		try {
				EFormData data = new KbeeEMemMemberData(form, member);
				for (EFormField<?> field : form.getFields()) {
					field.get(member, data);
				}	
				ApiProxy iform = new ApiProxy();
				iform.setName(form.getDisplayName());
				idata.setForm(iform);
				idata.setSigned(data.isSigned());
				idata.setData(getData(data));
				idata.setValue(new ApiProxy(String.valueOf(member.getId()), member.getDisplayName(), UriHelper.getUri(member), "value"));
		}
		catch (Exception e) {
			logger.error(e);
		}
		return idata;
	}
	
	private EForm getForm(DataSetMember member) {
		EForm form = member instanceof PersonMember ? new KbeeUserForm((PersonMember)member) : new KbeeMemberForm(member);
		return form;
	}
	
	protected String getSubline(DataSetMember value) {
		ExtractionRule rule = value.getDataSet().getSublineRule();
		if (rule!=null) {
			String label = (String)rule.extract(value);
			return label;
		}
		return null;
	}
	
	private List<IFieldData> getData(EFormData formdata) {
		List<IFieldData> data = (new IFormDataAdapter()).adapt(formdata);
		return data;
	}
}
