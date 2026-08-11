package com.novamens.kbee.content.webapi.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.form.EAutoCompleteField;
import com.novamens.content.form.EComboField;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.Suggestion;
import com.novamens.wicket.model.ObjectModel;

import kbee.api.model.IFieldValue;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.logging.Logger;

public class FileFormFieldOptionsHandler extends FileFormAbstractHandler {
	
	static private Logger kblogger = new Logger(FileFormFieldOptionsHandler.class.getName());
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	@SuppressWarnings("unchecked") 
	public List<IFieldValue> getOptions(long fileId, long eFormId, String fieldName, String pattern) {
		
		try {
			Content content = getContent(fileId);
			
			EForm eform = getForm(eFormId);
			
			EFormField<?> field = eform.getField(fieldName);
			
			if (field == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.INVALID_ATTRIBUTE);
			}
			
			List<DataSetMember> members;
			if (field instanceof EAutoCompleteField<?>) {
				members = new ArrayList<>();
				for (Suggestion suggestion : ((EAutoCompleteField<DataSetMember>)field).getChoicesSource(content).getValues(pattern)) {
					DataSetMember member = (DataSetMember)((ObjectModel<?>)suggestion.getObject()).getObject();
					members.add(member);
				};
			}
			else {
				members = ((EComboField<DataSetMember>)field).getChoicesSource(content).getValues();
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
	
	private IFieldValue getValue(Object object) {
		IFieldValue value = new IFieldValue();
		value.setUri(String.valueOf(((DataSetMember)object).getId()));
		value.setDisplayName(((DataSetMember)object).getDisplayName());
		value.setType("member");
		value.setId(String.valueOf(((DataSetMember)object).getId()));
		return value;
	}		
}