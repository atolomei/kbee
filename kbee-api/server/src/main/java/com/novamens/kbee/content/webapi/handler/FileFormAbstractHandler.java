package com.novamens.kbee.content.webapi.handler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.form.EComponentType;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.ResourceAdded;
import com.novamens.content.form.ResourceRemoved;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.form.ValueUpdated;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.kbfs.KBFSResourceService;
import com.novamens.kbee.content.resource.KbeeResourceNode;
import com.novamens.kbfs.FileServerException;

import kbee.api.model.ApiProxy;
import kbee.api.model.IFieldData;
import kbee.api.model.IFieldValue;
import kbee.api.model.IFormData;
import kbee.api.model.IFormTransaction;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;

public abstract class FileFormAbstractHandler extends AbstractRequestHandler {
	
	public class KbeeEValidatable implements EValidatable {
		EFormData data;
		EFormField<?> field;
		Map<String, String> errors;
		public KbeeEValidatable(EFormData data, EFormField<?> field, Map<String, String> errors) {
			this.data = data;
			this.field = field;
			this.errors = errors;
		}
		public Object getValue() {
			return getData().getData(getField());
		}
		public EFormField<?> getField() {
			return field;
		}
		public EFormData getData() {
			return data;
		}
		public void error(String key, String... parameter) {
			errors.put(getField().getName(), key);
		}
		public void error(String key) {
			errors.put(getField().getName(), key);
		}
	}

	protected List<UpdatedField> update(IFormData idata, EFormData edata, Object object) {
		List<UpdatedField> updates = new ArrayList<>();
		EForm eform = edata.getForm();
		for (IFieldData fielddata : idata.getData()) {
			if (EComponentType.COMBO.getLabel().equals(fielddata.getType()) || "AutoComplete".equals(fielddata.getType())) {
				EFormField<?> field = eform.getField(fielddata.getName());
				DataSetMember member = (DataSetMember) edata.getData(field);
				if (!fielddata.getValues().isEmpty()) {
					IFieldValue value = fielddata.getValues().get(0);
					DataSetMember memberupdated = getMember(value);
					if (memberupdated != null && !memberupdated.equals(member)) {
						edata.setData(field, memberupdated);
						updates.add(new ValueUpdated(eform, field.getLabel(), member, memberupdated));
					}
				}
				else {
					if (member!=null) {
						edata.setData(field, null);
						updates.add(new ValueUpdated(eform, field.getLabel(), member, null));
					}
				}
			}
			if (EComponentType.DATE.getLabel().equals(fielddata.getType())) {
				if (!fielddata.getValues().isEmpty()) {
					IFieldValue value = fielddata.getValues().get(0);
					OffsetDateTime dateupdated = value.getDate();
					EFormField<?> field = eform.getField(fielddata.getName());
					OffsetDateTime date = (OffsetDateTime) edata.getData(field);
					if (dateupdated != null && !dateupdated.equals(date)) {
						edata.setData(field, dateupdated);
						updates.add(new ValueUpdated(eform, field.getLabel(), date, dateupdated));
					}
				}
			}
			if ("resource".equals(fielddata.getType().toLowerCase())) {
				EFormField<?> field = eform.getField(fielddata.getName());
				Resource resource = (Resource)edata.getData(field);
				if (!fielddata.getValues().isEmpty()) {
					IFieldValue value = fielddata.getValues().get(0);
					Resource resourceupdated = getResource(value);
					if (resource==null || !equals(resourceupdated, resource)) {
						edata.setData(field, resourceupdated);
						updates.add(new ResourceAdded(eform, field.getLabel(), resourceupdated));
					}
					else {
						if (resource.getId()!=resourceupdated.getId()) {
							delete (resourceupdated);
						}
					}
				}
				else {
					if (resource!=null) {
						edata.setData(field, null);
						updates.add(new ResourceRemoved(eform, field.getLabel(), resource));
					}
				}
			}
			if ("resources".equals(fielddata.getType().toLowerCase()) || 
					"resource system".equals(fielddata.getType().toLowerCase())) {
				if (!fielddata.getValues().isEmpty()) {
					List<Resource> resourcesupdated = getResources(fielddata);
					EFormField<?> field = eform.getField(fielddata.getName());
					@SuppressWarnings("unchecked")
					List<Resource> resources = (List<Resource>)edata.getData(field);
					List<Resource> differences1, differences2;
					if (resources!=null) {
						differences1 = differences(resourcesupdated, resources);
						differences2 = differences(resources, resourcesupdated);
					}
					else {
						differences1 = resourcesupdated;
						differences2 = new ArrayList<>();
					}
					if (resources==null || !differences1.isEmpty() || !differences2.isEmpty()) {
						edata.setData(field, getNodes(resourcesupdated));
						for (Resource resource : differences1) { 
							updates.add(new ResourceAdded(eform, field.getLabel(), resource));
						}
						for (Resource resource : differences2) { 
							updates.add(new ResourceRemoved(eform, field.getLabel(), resource));
						}
					}

				}
			}
			if ("Html".equals(fielddata.getType()) || "Text".equals(fielddata.getType())
					|| "String".equals(fielddata.getType())) {
				if (!fielddata.getValues().isEmpty()) {
					IFieldValue value = fielddata.getValues().get(0);
					String textupdated = value.getDisplayName();
					if ("Html".equals(fielddata.getType()))
						textupdated = textupdated.replace("\n", "<br/>");
					EFormField<?> field = eform.getField(fielddata.getName());
					String text = (String) edata.getData(field);
					if (textupdated != null && !textupdated.equals(text)) {
						edata.setData(field, textupdated);
						updates.add(new ValueUpdated(eform, field.getLabel(), text, textupdated));
					}
				}
			}
			if (EComponentType.BOOLEAN.getLabel().equals(fielddata.getType())) {
				if (!fielddata.getValues().isEmpty()) {
					IFieldValue value = fielddata.getValues().get(0);
					Boolean booleanvalue = "true".equals(value.getId()) ? Boolean.TRUE : Boolean.FALSE;
					EFormField<?> field = eform.getField(fielddata.getName());
					Boolean fieldvalue = (Boolean)edata.getData(field);
					if (!booleanvalue.equals(fieldvalue)) {
						edata.setData(field, booleanvalue);
						updates.add(new ValueUpdated(eform, field.getLabel(), fieldvalue, booleanvalue));
					}
				}	
			}

		}
		return updates;
	}

	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Map<String, String> validate(EFormData data) {
		EForm eform = data.getForm();
		Map<String, String> errors = new HashMap<>();
		for (EFormField<?> field : eform.getFields()) {
			field.validate(new KbeeEValidatable(data, field, errors)); 
		}
		return errors;
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Content getContent(Long id) {
		Content content = null;

		content = getContentDao().findContentById(id);

		if (content == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FILE_NOT_FOUND);
		}

		return content;
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Object getData(IFieldData fielddata) {
		List<Object> values = new ArrayList<>();
		for (IFieldValue value : fielddata.getValues()) {
			values.add(getValue(value));
		}
		return values.size() == 1 ? values.get(0) : values;
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Object getValue(IFieldValue fieldvalue) {
		Object value = null;
		if ("member".equals(fieldvalue.getType())) {
			value = getMember(fieldvalue.getUri());
		}
		if ("date".equals(fieldvalue.getType())) {
			value = fieldvalue.getDate();
		}
		return value;
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected EForm getForm(long id) {
		EForm eform = getRepository(EForm.class).findById(id);
		if (eform == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA, null, "eform " + id);
		}
		return eform;
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected DataSetMember getMember(String id) {
		try {
			DataSetMember member = getContentDao().findMemberById(Long.valueOf(id));
			if (member == null) {
				throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA, null, "member " + id);
			}
			return member;
		} 
		catch (Exception e) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA, null, "member " + id);
		}
	}
	
	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected Resource getResource(IFieldValue value) {
		try {
			Resource resource = getContentDao().findResourceById(KBFile.class, Long.valueOf(value.getId()));
			return resource;
		} 
		catch (Exception e) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA, null, "resource " +  value.getId());
		}
	}
	
	protected List<Resource> getResources(IFieldData data) {
		List<Resource> resources = new ArrayList<>();
		for (IFieldValue value : data.getValues()) {
			Resource resource = getResource(value);
			resources.add(resource);
		}
		return resources;
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected DataSetMember getMember(IFieldValue value) {
		if (value.getUri() == null)
			return null;
		DataSetMember member = getMember(Long.valueOf(value.getUri()));
		return member;
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected DataSetMember getMember(long id) {
		DataSetMember member = getContentDao().findMemberById(id);
		if (member == null) {
			throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA, null, "member " + id);
		}
		return member;
	}
	
	
	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	
	protected List<Resource> differences(List<Resource> resources1, List<Resource> resources2) {
		List<Resource> differences = new ArrayList<>();
		for (Resource resource1 : resources1) {
			boolean found = false;
			for (Resource resource2 : resources2) {
				if (equals(resource1,resource2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				differences.add(resource1);
			}
		}
		return differences;
	}
	
	protected List<ResourceNode> getNodes(List<Resource> resources) {
		List<ResourceNode> nodes = new ArrayList<>();
		for (Resource resource : resources) {
			nodes.add(new KbeeResourceNode(resource, null));
		}
		return nodes;
	}
	
	protected boolean equals(Resource resource1, Resource resource2) {
		if (resource1.getId().equals(resource2.getId()))
			return true;
		
		if (resource1 instanceof ResourceNode) {
			resource1 = ((ResourceNode)resource1).getResource();
		}
		
		if (resource2 instanceof ResourceNode) {
			resource2 = ((ResourceNode)resource2).getResource();
		}
		
		KBFile file1 = (KBFile)resource1;
		KBFile file2 = (KBFile)resource2;
		
		if (file1.getSize()!=file2.getSize()) 
			return false;
		
		String sha1 = file1.getSHA256();
		String sha2 = file2.getSHA256();

		if (sha1!=null && sha2!=null)
			return sha1.equals(sha2);
			
		return false;
	}
	
	/**
	 * ------------------------------------------------------------------------------------------------------------------------
	 */
	protected void delete(Resource resource) {
		try {
			KBFile file = (KBFile)resource;
			KBFSResourceService service = file.getService(KBFSResourceService.class);
			service.removeObject();
			getContentDao().delete(file);
		}
		catch (FileServerException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.INTERNAL_ERROR, e.getMessage());
		}
	}
	
	
	/** ------------------------------------------------------------------------------------------------------------------------
	 */
	protected ITransaction getFormTransaction(ApiProxy proxy, Map<String, String> errors) {
		IFormTransaction transaction = new IFormTransaction();
		transaction.setId((long)0);
		transaction.setTarget(proxy);
		transaction.setErrors(errors);
		return transaction;
	}
}