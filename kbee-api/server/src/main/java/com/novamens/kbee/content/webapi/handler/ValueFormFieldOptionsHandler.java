package com.novamens.kbee.content.webapi.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.form.EAutoCompleteField;
import com.novamens.content.form.EComboField;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.content.form.KbeeMemberForm;
import com.novamens.kbee.content.form.KbeeUserForm;
import com.novamens.wicket.model.ObjectModel;

import kbee.api.model.IFieldValue;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.logging.Logger;

public class ValueFormFieldOptionsHandler extends FileFormAbstractHandler {
	
	static private Logger kblogger = new Logger(ValueFormFieldOptionsHandler.class.getName());
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	@SuppressWarnings("unchecked") 
	public List<IFieldValue> getOptions(long valueId, String fieldName, String pattern) {
		
		try {
			

			DataSetMember eformmember = getMember(valueId);
			
			EFormData edata = getFormData(eformmember);
			
			EForm eform = edata.getForm();
			
			EFormField<?> field = eform.getField(fieldName);
			
			if (field == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.INVALID_ATTRIBUTE);
			}
			
			List<DataSetMember> members;
			if (field instanceof EAutoCompleteField<?>) {
				members = new ArrayList<>();
				for (Suggestion suggestion : ((EAutoCompleteField<DataSetMember>)field).getChoicesSource(eformmember).getValues(pattern)) {
					DataSetMember member = (DataSetMember)((ObjectModel<?>)suggestion.getObject()).getObject();
					members.add(member);
				};
			}
			else {
				members = ((EComboField<DataSetMember>)field).getChoicesSource(eformmember).getValues();
			}
			
			List<IFieldValue> values = new ArrayList<>();
			
			for (DataSetMember member : members) {
				if (values.size()>100) 
					break;
				else
				values.add(getValue(member));
			}
			
			return values;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (Exception e) {
			kblogger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}	
	}
	
	private EFormData getFormData(DataSetMember member) {
		EForm form = getForm(member);
		EFormData data = new KbeeEMemMemberData(form, member);
		for (EFormField<?> field : form.getFields()) {
			field.get(member, data);
		}	
		return data;
	}
	
	private EForm getForm(DataSetMember member) {
		return member instanceof PersonMember ? new KbeeUserForm((PersonMember)member) : new KbeeMemberForm(member);
	}
	
	private IFieldValue getValue(Object object) {
		IFieldValue value = new IFieldValue();
		value.setUri(String.valueOf(((DataSetMember)object).getId()));
		value.setDisplayName(((DataSetMember)object).getDisplayName());
		value.setType("member");
		return value;
	}		
}