package com.novamens.kbee.content.webapi.handler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;

import kbee.api.model.ApiValue;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class ValueCreateHandler extends ValueUpdateHandler {

	@Transactional
	public ITransaction create(ApiValue value) {
		return update(value);
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	protected DataSetMember getValue(ApiValue value) throws ContentMgmtException {
		DataSetMember member = null;
		DataSet dataset = getDataSet(value.getDataSet());
		if (dataset==null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DATASET_NOT_FOUND);
		}
		if (value.getDomain()==null || !dataset.getDomain().getName().toLowerCase().equals(value.getDomain().toLowerCase())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.DATASET_NOT_FOUND);
		}
		member = dataset.createMember();
		return member;
	}
	
	protected List<String> update(DataSetMember member, ApiValue value) {
		List<String> updates = super.update(member, value);
		if (updates.isEmpty())
		updates.add("value");
		return updates;
	}
}