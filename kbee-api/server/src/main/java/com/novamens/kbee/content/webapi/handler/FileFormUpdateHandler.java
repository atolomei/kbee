package com.novamens.kbee.content.webapi.handler;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.model.ExtractionRule;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.lock.LockTransactionSynchronization;

import kbee.api.model.IFormData;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class FileFormUpdateHandler extends FileFormAbstractHandler {
	
	static private Logger logger = LogManager.getLogger(FileUpdateAbstractHandler.class.getName());
	static private kbee.util.logging.Logger kblogger = new kbee.util.logging.Logger(logger);
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction update(IFormData idata, boolean validation) {
		
		String fileId = null;
		
		try {
			
			fileId = idata.getFile().getId();
			
			new LockTransactionSynchronization(fileId);

			Content content = getContent(Long.valueOf(fileId));
			
			EForm eform = getForm(Long.valueOf(idata.getForm().getId()));
			
			EFormData edata = content.getFormData(new KbeeTaskForm(eform));
			
			List<UpdatedField> updates = update(idata, edata, content);
			
			Map<String, String> errors = null;
			if (!updates.isEmpty()) {
				errors = validation ? validate(edata) : null;
				if (!validation || errors.isEmpty()) {
	 				for (EFormField<?> field : edata.getForm().getFields()) {
						field.set(content, edata);
					}
	 				applyTitleRule(content);
	 				content.getService(ContentService.class).updateFields(updates);
				}
			}	
		
			ITransaction transaction  = getFormTransaction(getProxy(content), errors);
			
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
	
	private void applyTitleRule(Content content) {
		ExtractionRule rule = content.getContentTemplate().getTitleRule();
		String title = (String)rule.extract(content);
		content.setTitle(title);
	}
}