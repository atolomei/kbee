package com.novamens.kbee.content.webapi.handler;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EResourceField;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.FileService;
import com.novamens.content.user.SignatureType;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.user.UserSignature;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.service.ServiceLocator;
import com.novamens.signature.SignatureException;
import com.novamens.workflow.Activity;

import kbee.api.model.ISignedData;
import kbee.api.model.ITransaction;
import kbee.api.service.ApiError;
import kbee.api.service.ApiException;
import kbee.util.PropertiesFactory;

public class SignatureHandler extends AbstractRequestHandler {

	static private ObjectMapper mapper = new ObjectMapper();
	static  {
		//smapper.registerModule(new JavaTimeModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	private String server = (String) PropertiesFactory.getInstance("kbee").getProperties().get("server");
	private String port = (String) PropertiesFactory.getInstance("kbee").getProperties().get("port");

	@Autowired
	Environment environment;
	
	@Transactional
	public ITransaction sign(String activityId, String eform, String deviceId) {
	//public ITransaction sign(EFormData data, String deviceId) {
		try {
			
	        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)findActivityById(Long.valueOf(activityId));
	        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
	        }
	        
	        EForm form = findFormById(Long.valueOf(eform));
	        if (form==null) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FORM_NOT_FOUND);
	        }
	        
	        EFormData data = activity.getContent().getFormData(new KbeeTaskForm(form));
	        if (data==null) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA);
	        }
	        
			UserSignature signature = getSignature(SignatureType.PHONE_APP, deviceId);
			if (signature == null) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.SIGNATURE_NOT_FOUND);
			}
			
			UserDevice device = getDevice(deviceId);
			if (device==null || !ObjectState.ENABLED.equals(device.getState())) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.DEVICE_NOT_REGISTERED);
			}
			
			Content content = data instanceof EFormContentData ? ((EFormContentData)data).getContent() : null; 
			if (content==null) {
				throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.ACCESS_DENIED);
			}
			
			content.getService(ContentService.class).sign(data, getDigest(data), signature, device, getSignatureHtmlStream(signature));
			ITransaction transaction  = getTransaction(getProxy(content));
			
			return transaction;
		}
		catch (IOException | SignatureException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.ACCESS_DENIED);
		}
	}
	
	@Transactional
	public ITransaction update(String activityId, String eform, String resourceId, String deviceId, ISignedData signedData) {
		try {
			
	        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)findActivityById(Long.valueOf(activityId));
	        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
	        }
	        
	        if (!activity.getUser().equals(getUser())) {
	            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
	        }
	        
	        EForm form = findFormById(Long.valueOf(eform));
	        if (form==null) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FORM_NOT_FOUND);
	        }
	        
	        EFormData data = activity.getContent().getFormData(new KbeeTaskForm(form));
	        if (data==null) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA);
	        }   


			UserSignature signature = getSignature(SignatureType.PHONE_APP, deviceId);
			if (signature == null) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.SIGNATURE_NOT_FOUND);
			}
			
			UserDevice device = getDevice(deviceId);
			if (device==null || !ObjectState.ENABLED.equals(device.getState())) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.DEVICE_NOT_REGISTERED);
			}
			
			Content content = data instanceof EFormContentData ? ((EFormContentData)data).getContent() : null; 
			if (content==null) {
				throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.ACCESS_DENIED);
			}
			
			if (resourceId!=null) {
				// tiene que ser un file container valido: un file y solo un file en el form
				KBFile signedFile = findResourceById(Long.valueOf(resourceId));
				if (signedFile==null) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.RESOURCE_NOT_FOUND);
				}
				boolean found = false;
				for (EFormField<?> field : data.getForm().getFields()) {
					if (field instanceof EResourceField) {
						if (!found) {
							data.setData(field, signedFile);
							((EResourceField)field).set(content, data);
							found = true;
						}
						else {
							throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.DEVICE_NOT_REGISTERED);
						}
					}
				}
				if (!found) {
					throw new ApiException(HttpStatus.NOT_FOUND, ApiError.RESOURCE_NOT_FOUND);
				}
				else {
					
				}
			}
			
			try {
				content.getService(ContentService.class).sign(data, signedData.getData(), signedData.getSignedData(), signature, device, getSignatureHtmlStream(signature));
			}
			catch (SignatureException e) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.INVALID_SIGNATURE);
			}
			
			ITransaction transaction  = getTransaction(getProxy(content));
			
			return transaction;
		}
		catch (Exception e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.ACCESS_DENIED);
		}
	}
	
	@Transactional
	public ITransaction getSigned(String activityId, String eform, String resourceId, String deviceId) {
		try {
			
 	        KbeeWorkflowActivity activity = (KbeeWorkflowActivity)findActivityById(Long.valueOf(activityId));
	        if (activity==null || !activity.getContent().getDomain().equals(getDomain())) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.ACTIVITY_NOT_FOUND);
	        }
	        
	        if (!activity.getUser().equals(getUser())) {
	            throw new ApiException(HttpStatus.FORBIDDEN, ApiError.ACCESS_DENIED);
	        }
	        
	        EForm form = findFormById(Long.valueOf(eform));
	        if (form==null) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.FORM_NOT_FOUND);
	        }
	        
	        EFormData data = activity.getContent().getFormData(new KbeeTaskForm(form));
	        if (data==null) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.NO_DATA);
	        }

			UserSignature signature = getSignature(SignatureType.PHONE_APP, deviceId);
			if (signature == null) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.SIGNATURE_NOT_FOUND);
			}
			
			UserDevice device = getDevice(deviceId);
			if (device==null || !ObjectState.ENABLED.equals(device.getState())) {
				throw new ApiException(HttpStatus.PRECONDITION_FAILED, ApiError.DEVICE_NOT_REGISTERED);
			}
			
			
			Content content = data instanceof EFormContentData ? ((EFormContentData)data).getContent() : null; 
			if (content==null) {
				throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.ACCESS_DENIED);
			}
			
			Resource resource = null; 
			for (Resource contentresource : ((ResourceContainer)content).getResources()) {
				if (resourceId.equals(String.valueOf(contentresource.getId()))) {
					resource = contentresource;
					break;
				}
			}
			
			if (resource==null || !(resource instanceof KBFile)) {
	            throw new ApiException(HttpStatus.NOT_FOUND, ApiError.RESOURCE_NOT_FOUND);
			}
			
			KBFile signed = ((KBFile)resource).getService(FileService.class).getSigned(signature, getSignatureHtmlStream(signature));
			
			ITransaction transaction  = getTransaction(getProxy(signed));
			
			return transaction;
		}
		catch (Exception e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiError.ACCESS_DENIED);
		}	
	}

	
	private UserSignature getSignature(SignatureType type, String deviceId) {
		for (UserSignature signature : getUserProfile().getSignatures()) {
			if (type.equals(signature.getType()) && 
					ObjectState.ENABLED.equals(signature.getState()) &&
					signature.getDevice()!=null &&
					deviceId.equals(signature.getDevice().getDeviceId())) {
				return signature;
			}
		}
		return null;
	}
	
	private UserDevice getDevice(String deviceId) {
        for (UserDevice device : getUserProfile().getDevices()) {
        	if (device.getDeviceId().equals(deviceId)) {
        		return device;
        	}
        }
        return null;
	}
	
	private Activity findActivityById(long id) {
		Activity activity = getWorkflowDao().findActivityById(id);
		return activity;
	}
	
	private EForm findFormById(long id) {
		EForm form  = getRepository(EForm.class).findById(id);
		return form;
	}
	
	private KBFile findResourceById(long id) {
		KBFile file = (KBFile)getContentDao().findResourceById(KBFile.class, id);
		return file;
	}	
	
	private UserProfile getUserProfile() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile();
	}
	
	private String getSignatureHtmlStream(UserSignature signature) throws IOException {
		String servername = server;
		if (port!=null) servername +=":"+port;
		String url = servername+"/sharedsignature/"+String.valueOf(signature.getUser().getId())+"/"+signature.getDevice().getDeviceId();
		java.io.InputStream input = new URL(url).openStream();
		String stream = IOUtils.toString(input, StandardCharsets.UTF_8);
		stream = stream.replace("&amp;","&");
		return stream;
 	}
	
	private String getDigest(EFormData data) {
		String value = "";
		for (EFormField<?> field : data.getForm().getFields()) {
			if (!"".equals(value)) value+=";";
			value += field.getName() + ":";
			Object fieldobject = data.getData(field);
			String fieldvalue = DisplayNameExtractor.get(fieldobject);
			value+=fieldvalue;
		}
		return value;
//		try {
//			//String json = mapper.writeValueAsString(data);
//			String json =data.toString();
//			return json;
//		}
//		catch (JsonProcessingException e) {
//			throw new KbeeRuntimeException(e);
//		}
	}
}