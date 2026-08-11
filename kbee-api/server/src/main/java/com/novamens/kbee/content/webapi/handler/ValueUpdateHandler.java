package com.novamens.kbee.content.webapi.handler;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.webapi.type.UriHelper;

import kbee.api.model.ApiProxy;
import kbee.api.model.ApiValue;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public class ValueUpdateHandler extends ClassificableUpdateHandler {
	
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ValueUpdateHandler.class.getName());
	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Transactional
	public ITransaction update(ApiValue value) {
		try {
			Domain domain = getDomain(value);
			
			if (domain == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.DOMAIN_NOT_FOUND);
			}
			
			su(domain);
						
			// // System.out.println(getDomain().getName());
			
			DataSetMember member = getValue(value);
			
			List<String> updates = new ArrayList<String>();
 			
			updates.addAll(update(member, value));
	
			updates.addAll(setAttributes(member, value));
						
			if (updates.isEmpty()) {
				throw new ApiException(HttpStatus.NOT_MODIFIED, ApiError.NOT_MODIFIED);
			}
			
			getContentDao().flush();
			
			member.getService(DOMObjectService.class).update(updates);
			
			ITransaction transaction  = getTransaction(getProxy(member));
			
			return transaction;
		}
		catch (ApiException e) {
			throw e;
		}
		catch (ContentMgmtException e) {
			
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
		catch (AuthenticationException e) {
			logger.error(e);
			throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED, e.getMessage());
		}
		catch (Exception e) {
			logger.error(e);
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	protected List<Classifier> getClassifiers(Classificable classificable){
		return ((DataSetMember)classificable).getDataSet().getClassifiers();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	@Override
	protected List<AttributeTemplate> getAttributes(Classificable classificable) {
		return ((DataSetMember)classificable).getDataSet().getAttributes();
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ApiProxy getProxy(DataSetMember member) {
		return new ApiProxy(String.valueOf(member.getId()), member.getDisplayName(), UriHelper.getUri(member), "value");
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected DataSetMember getValue(ApiValue value) throws ContentMgmtException {
		DataSetMember member = null;
		if (value.getId()!=null) {
			try {
				member = getContentDao().findMemberById(Long.valueOf(value.getId()));
				if (member==null || !member.getDataSet().equals(getDataSet(value.getDataSet()))) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND);
				}
			}
			catch (NumberFormatException e) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND, e.getMessage());
			}
		}
		else {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.VALUE_NOT_FOUND, "NO ID");
		}
		return member;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected List<String> update(DataSetMember member, ApiValue value) {
		List<String> updates = new ArrayList<String>();
		
		if (!equals(member.getDisplayName(), value.getDisplayName())) {
			if (value.getDisplayName()==null || "".equals(value.getDisplayName().trim())) {
				throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.INVALID_ATTRIBUTE, "invalid value");
			}
			List<DataSetMember> others = getContentDao().findMembersByValue(member.getDataSet(), value.getDisplayName());
			for (DataSetMember other : others) {
				if (other!=null && !other.equals(member)) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.VALUE_ALREADY_EXIST, "display name");
				}
			}
			member.setStrValue(value.getDisplayName());
			updates.add("value");
		}
		
		if (!equals(member.getExternalId(), value.getExternalId())) {
			if (value.getExternalId()!=null) {
				DataSetMember other = getContentDao().findMemberByExternalId(value.getExternalId());
				if (other!=null && !other.equals(member)) {
					throw new ApiException(HttpStatus.BAD_REQUEST, ApiError.VALUE_ALREADY_EXIST, "exteranal id");
				}
			}
			member.setExternalId(value.getExternalId());
			updates.add("external id");
		}
		
		return updates;
	}
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected DataSet getDataSet(ApiProxy proxy) {
		if (proxy==null) return null;
		String domainname = "";
		String id = proxy.getId();
		if (id == null) {
			if (proxy.getHRef()==null)
				return null;
			String href = proxy.getHRef();
			if (!href.startsWith("/")) href = "/"+href;
			String urlfrags[] = proxy.getHRef().split("/");
			domainname = urlfrags[1];
			id = urlfrags[urlfrags.length-1];
		}
		if (StringUtils.isNumeric(id)) {
			DataSet dataset = (DataSet)getContentDao().findModelObjectById(DataSet.class, id);
			return dataset;
		}
		else {
			Domain domain = getContentDao().findDomainByName(domainname);
			if (domain==null) return null;
			id = id.toLowerCase();
			for (DataSet dataset : getContentDao().getDataSets(domain)) {
				if (dataset.getAlias()!=null && id.equals(dataset.getAlias().toLowerCase())) {
					return dataset;
				}
				if (id.equals(dataset.getName().toLowerCase())) {
					return dataset;
				}
			}
			return null;
		}
	}
}