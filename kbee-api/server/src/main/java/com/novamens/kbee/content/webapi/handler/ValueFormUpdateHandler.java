package com.novamens.kbee.content.webapi.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.content.form.KbeeMemberForm;
import com.novamens.kbee.content.form.KbeeUserForm;
import com.novamens.kbee.content.webapi.type.UriHelper;
import com.novamens.kbee.lock.LockTransactionSynchronization;

import kbee.api.model.ApiProxy;
import kbee.api.model.IFormData;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class ValueFormUpdateHandler extends FileFormAbstractHandler {
	
	static private Logger logger = LogManager.getLogger(FileUpdateAbstractHandler.class.getName());
	static private kbee.util.logging.Logger kblogger = new kbee.util.logging.Logger(logger);
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction update(IFormData idata) {
		
		String valueId = null;
		
		try {
			
			valueId = idata.getValue().getId();
			
			new LockTransactionSynchronization(valueId);

			DataSetMember member = getMember(Long.valueOf(valueId));
			
			EFormData edata = getFormData(member);
			
			EForm eform = edata.getForm();
			
			List<UpdatedField> updates = update(idata, edata, member);
			
			Map<String, String> errors = null;
			if (!updates.isEmpty()) {
				errors = validate(edata);
				if (errors.isEmpty()) {
					for (EFormField<?> field : eform.getFields()) {
						field.set(member, edata);
					}
					member.getService(DOMObjectService.class).update(getUpdatedParts(updates));
				}
			}
		
			ITransaction transaction  = getFormTransaction(getProxy(member), errors);
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error(e);
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
	
	private List<String> getUpdatedParts(List<UpdatedField> updates) {
		List<String> updatedParts = new ArrayList<>();
		for (UpdatedField update : updates) {
			updatedParts.add(update.getLabel());
		}
		return updatedParts;
	}
	
	private ApiProxy getProxy(DataSetMember member) {
		return new ApiProxy(member.getDisplayName(), UriHelper.getUri(member));
	}
 }