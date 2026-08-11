package com.novamens.kbee.content.webapi.type;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.base.SecurityRule;
import com.novamens.content.command.Command;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.LauncherGroup;
import com.novamens.content.model.PersonMember;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.Role;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Procedure;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiUser;
import kbee.api.model.IWorkflowEvent;

public class UriHelper {
	private static UriHelper Instance;
	
	public static UriHelper getInstance() {
		if (Instance==null) 
			Instance = new UriHelper();
		return Instance;
	}
	
	public static String getUri(Object object) {
		return getInstance()._getUri(object, false);
	}
	
	public static String getUri(Object object, boolean internal) {
		return getInstance()._getUri(object, internal);
	}
	
	public static String getUri(Content content, Resource resource) {
		return getInstance()._getUri(content, resource);
	}
	
	public static String getUri(Resource resource) {
		return getInstance()._getUri(resource);
	}
	
	public String _getUri(Object object, boolean internal) {
		if (object instanceof Group) {
			String domain = ((KbeeGroup)object).getDomain().getName();
			return "/" + domain + "/groups/" + String.valueOf(((Group )object).getId());
		}
		
		if (object instanceof Role) {
			String domain = ((Role)object).getDomain().getName();
			return "/" + domain + "/roles/" + String.valueOf(((Identifiable)object).getId());
		}
		
		if (object instanceof Classifier) {
			String domain = ((DomainObject)object).getDomain().getName();
			return "/" + domain+"/classifiers/" + String.valueOf(((Classifier)object).getId());
		}
		
		if (object instanceof Procedure) {
			String domain = ((DomainObject)object).getDomain().getName();
			return "/" + domain+"/procedures/" + String.valueOf(((Procedure)object).getId());
		}
		
		if (object instanceof Attribute) {
			String domain = ((DomainObject)object).getDomain().getName();
			return "/" + domain+"/attributes/" + String.valueOf(((Attribute)object).getId());
		}
		
		if (object instanceof ResourceTag) {
			String domain = ((DomainObject)object).getDomain().getName();
			return "/" + domain+"/resourceTags/" + String.valueOf(((Identifiable)object).getId());
		}
		
		if (object instanceof LauncherGroup) {
			String domain = ((DomainObject)object).getDomain().getName();
			return "/" + domain+"/launchergroups/" + String.valueOf(((Identifiable)object).getId());
		}
		
		if (object instanceof DataSet) {
			DataSet dataset = (DataSet)object;
			String domain = ((DomainObject)object).getDomain().getName();
			String uri;
			if (dataset.getAlias()!=null)
				uri = "/" + domain+"/datasets/"+String.valueOf(dataset.getAlias());
			else
				uri = "/" + domain+"/datasets/"+String.valueOf(dataset.getId());
			return uri;
		}
		
		if (object instanceof ContentTemplate) {
			String domain = ((DomainObject)object).getDomain().getName();
			return "/" + domain+"/templates/" + String.valueOf(((ContentTemplate)object).getId());
		}
		
		if (object instanceof Domain) {
			return "domain/"+String.valueOf(((Domain)object).getId());
		}
		
		if (object instanceof User) {
			return "/users/" + String.valueOf(((User)object).getId());
		}
		
		if (object instanceof ApiUser) {
			ApiUser user = (ApiUser)object;
			String uri = "/";
			uri += user.getDomain()!=null ? user.getDomain() + "/" :"";
			uri += "users/";
			uri += user.getId()!=null ? user.getId() : "newuser";
			return uri;
		}
		
		if (object instanceof DataSetMember) {
			DataSetMember member = (DataSetMember)object;
			String domain = member.getDomain().getName().toLowerCase();
			String dataset = member.getDataSet().getAlias()!=null? member.getDataSet().getAlias().toLowerCase() : member.getDataSet().getName().toLowerCase();
			String url = "/" + domain + "/datasets/" + dataset + "/values/" + String.valueOf(member.getId());
			return url;
		}
		
		if (object instanceof PersonMember) {
			Person person = (Person)object;
			String domain = person.getDomain().getName();
			String id = String.valueOf(person.getId());
			return "/"+ domain + "/users/" + id;
		}
		
		if (object instanceof Person) {
			Person person = (Person)object;
			String domain = person.getDomain().getName();
			String id = String.valueOf(person.getId());
			return "/"+ domain + "/persons/" + id;
		}
		
 		if (object instanceof SecurityRule) {
			String domain = ((SecurityRule)object).getDomain().getName();
			return domain + "/security/rules/" + String.valueOf(((SecurityRule)object).getId());
		}
 		
		if (object instanceof KBFile) {
			KBFile file = (KBFile)object;
			String url = "/resource/content/" + file.getId() + "/" + file.getName(); 
			return url;
		}
		
		if (object instanceof Resource) {
			return "resource/" + String.valueOf(((Resource)object).getId());
		}
		
		if (object instanceof EIdentifiableForm) {
			String domain = ((DomainObject)object).getDomain().getName();
			return "/"+ domain +"/forms/" + String.valueOf(((EIdentifiableForm)object).getId());
		}
		
		if (object instanceof Content) {
			String url = "";
			Content content = (Content)object;
			if (!internal && content.getExternalId()!=null) {
				url = "file/";
				url += getApplication(content) + "/";
				url += content.getDomain().getName() + "/";
				url += content.getExternalId();
			}
			else {
				url = "file/" + String.valueOf(content.getOId()) + "/" + String.valueOf(content.getId());
			}
			return url;
		}
		


		if (object instanceof ApiFile) {
			ApiFile file = (ApiFile)object;
			String url = "file/";
			if (!internal && file.getExternalId()!=null) {
				url +=  file.getApplication()!=null ? file.getApplication() +"/": "application/"; 
				url +=  file.getDomain()!=null ? file.getDomain()+"/" : "domain/"; 
				url +=  file.getExternalId()!=null ? file.getExternalId() : "id";
			}
			else {
				url += file.getOId() + "/"; 
				url += file.getId(); 
			}
			return url;
		}
		
		if (object instanceof IWorkflowEvent) {
			IWorkflowEvent event = (IWorkflowEvent)object;
			String url =  event.getDomain() + "/user/activities/" + event.getActivity();
			return url;
		}

		
		if (object instanceof Command) {
			String url = "command/"+((Command)object).getId();
			return url;
		}
		return null;
	}
	
	public String _getUri(Content content, Resource resource) {
		String uri = "";
		if (content.getSource()!=null && content.getExternalId()!=null) {
			uri += "/file/";
			uri += content.getSource().getName() + "/";
			uri += content.getDomain().getName() + "/";
			uri += content.getExternalId() + "/";
			uri += "resource/";
			uri += resource.getName();
		}
		else {
			uri += "/resource/content/";
			uri += (new ContentId(content)).toString() + "/";
			uri += resource.getName();
		}
		return uri;
	}
	
	public String _getUri(Resource resource) {
		String uri="/resourceref/"+ String.valueOf(resource.getId()) + "/" + resource.getName();
		String token = ServiceLocator.getService(SecurityService.class).nextSecureToken();
		ServiceLocator.getService(SecurityService.class).addToken(resource.getId(), token, 60);
		uri+="?token="+token;
		return uri;
	}	
	
	protected String getApplication(Content content) {
		if (content.getSource()!=null)
			return content.getSource().getName();
		else
			return "null";
	}
	
}
