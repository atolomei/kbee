package com.novamens.kbee.template;

import freemarker.template.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.SignedData;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserDevice;
import com.novamens.content.user.UserSignature;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.user.KbeeUserDevice;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ActivityProgressNote;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Task;

public abstract class KbeeObjectTemplateModel implements TemplateHashModelEx, TemplateNodeModel, TemplateScalarModel {
	
	private Map<String, KbeeMethod> canonicalsMap;
	private TemplateNodeModel parent;
	
	public KbeeObjectTemplateModel() {
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		TemplateModel model = null;
		KbeeMethod canonical = getCanonical(key);
		if (canonical!=null) {
			model = getModel(canonical);
		}
		if (".".equals(key)) {
			model = this;
		}
		return model;
	}
	
	public boolean isEmpty() throws TemplateModelException {
		return false;
	}
	
	public int size() throws TemplateModelException {
		return keysSet().size();
	}
	
	public TemplateCollectionModel keys() throws TemplateModelException {
		return new SimpleCollection(keysSet(), null);
	}
	
	public void setParentNode(TemplateNodeModel parent) {
		this.parent = parent;
	}
	
	public TemplateNodeModel getParentNode() throws TemplateModelException {
		return parent;
	}

	@Override
	public TemplateSequenceModel getChildNodes() throws TemplateModelException {
		return new SimpleSequence(getChilds(), null);
	}

	@Override
	public abstract String getNodeName() throws TemplateModelException;
	
	@Override
	public abstract String getNodeType() throws TemplateModelException;
	
	@Override
	public String getNodeNamespace() throws TemplateModelException {
		return null;
	}
	
	public abstract Object getObject();
	
	public String escape(String template) {
		return template;
	}
	
	protected TemplateModel wrap(KbeeMethod method, Object value) {
		TemplateModel model = null;
		if (value!=null && value instanceof TemplateModel) {
			return (TemplateModel)value;
		}
		else
		if (value!=null && value instanceof Person) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("name", "LastFirstName"));
			canonicals.add(new KbeeMethod("lastfirstname", "LastFirstName"));
			canonicals.add(new KbeeMethod("firstname", "FirstName"));
			canonicals.add(new KbeeMethod("lastname", "LastName"));
			canonicals.add(new KbeeMethod("email", "EMail"));
			canonicals.add(new KbeeMethod("domain", "Domain"));
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof KbeeUser) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("name", "LastFirstName"));
			canonicals.add(new KbeeMethod("username", "name"));
			canonicals.add(new KbeeMethod("lastfirstname", "LastFirstName"));
			canonicals.add(new KbeeMethod("firstname", "FirstName"));
			canonicals.add(new KbeeMethod("lastname", "LastName"));
			canonicals.add(new KbeeMethod("email", "EMail"));
			canonicals.add(new KbeeMethod("domain", "Domain"));
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof Number) {
			model = new KbeeNumberTemplateModel(method.getName(), (Number)value, null);
		}
		else
 		if (value!=null && value instanceof OffsetDateTime) {
			model = new KbeeDateTemplateModel(method.getName(), (OffsetDateTime)value, null);
		}
		else
		if (value!=null && value instanceof Domain) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("name", "Name"));
			canonicals.add(new KbeeMethod("organization", "Organization"));
			canonicals.add(new KbeeMethod("logo", "Logo"));
			model = new KbeeObjectWrapperTemplateModel(value, "Domain", canonicals, this);
		}
		else
		if (value!=null && value instanceof Content) {
			model = new KbeeContentTemplateModel((Content)value);
		}
		else
		if (value!=null && value instanceof ActivityProgressNote) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("text", "text"));
			canonicals.add(new KbeeMethod("time", "time"));
			canonicals.add(new KbeeMethod("author", "lastModifiedUser"));
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof Activity) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("task", "task"));
			canonicals.add(new KbeeMethod("startTime", "startTime"));
			canonicals.add(new KbeeMethod("dueDate", "dueDate"));
			canonicals.add(new KbeeMethod("note", "note"));
			canonicals.add(new KbeeMethod("url") {
				public Object evaluate(Object object) {
					return ((KbeeWorkflowActivity)object).getContent().getService(UrlService.class).getTaskUrl();
				};
			});
			canonicals.add(new KbeeMethod("publicUrl") {
				public Object evaluate(Object object) {
					KbeeTask task = (KbeeTask)((KbeeWorkflowActivity)object).getContent().getService(WorkflowService.class).getTask();
					if (task.isEnablePublicLink()) {
						return ((KbeeWorkflowActivity)object).getContent().getService(UrlService.class).getPublicTaskUrl();
					}
					else {
						return null;
					}
				};
			});
			canonicals.add(new KbeeMethod("user", "user"));
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof Task) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("displayName", "displayName"));
			canonicals.add(new KbeeMethod("procedure", "procedure"));
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof Procedure) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("displayName", "displayName"));
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof Resource) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("name", "Name"));
			canonicals.add(new KbeeMethod("title", "Title"));
			canonicals.add(new KbeeMethod("size", "Size"));
			canonicals.add(new KbeeMethod("thumbnail980") {
				public Object evaluate(Object object) {
					return ((Resource)object).getService(UrlService.class).getThumbnailUrl(ThumbnailSize.W980);
				};
			});
			canonicals.add(new KbeeMethod("url") {
				public Object evaluate(Object object) {
					return ((Resource)object).getService(UrlService.class).getUrl();
				};
			});
			canonicals.add(new KbeeMethod("publicurl") {
				public Object evaluate(Object object) {
					return ((Resource)object).getService(UrlService.class).getPublicUrl();
				};
			});
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof SignedData) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("date", "Date"));
			canonicals.add(new KbeeMethod("signature", "Signature"));
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof UserSignature) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("user", "User"));
			canonicals.add(new KbeeMethod("image", "HandWriteImage"));
			canonicals.add(new KbeeMethod("person") {
				public Object evaluate(Object object) {
					Person  person = ((UserSignature)object).getUserProfile().getPerson();
					PersonMember member = getMember(person);
					return member!=null ? new KbeeUserTemplateModel((PersonMember)member) : null;
				};
			});
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof UserDevice) {
			Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
			canonicals.add(new KbeeMethod("id", "id"));
			canonicals.add(new KbeeMethod("description", "description"));
			canonicals.add(new KbeeMethod("owner", "owner"));
			canonicals.add(new KbeeMethod("registrationUrl") {
				public Object evaluate(Object object) {
					KbeeUserDevice device = (KbeeUserDevice)object;
					KbeeJson data = new KbeeJson();
					data.put("id", String.valueOf(device.getDeviceId()));
					data.put("description", device.getDisplayName());
					data.put("number", device.getNumber());
					data.put("owner", String.valueOf(device.getUserProfile().getPerson().getId()));
					data.put("date", OffsetDateTime.now().toString());
					return device.getDomain().getService(UrlService.class).getServerUrl() + 
						"/registrationdevice/" + 
						ServiceLocator.getService(TokenService.class).getToken(data);
				};
			});
			model = new KbeeObjectWrapperTemplateModel(value, method.getName(), canonicals, this);
		}
		else
		if (value!=null && value instanceof DataSetMember) {
			model = new KbeeValueTemplateModel((DataSetMember)value);
		}
		else
		if (value!=null && value instanceof Serializable) {
			model = new KbeeCanonicalTemplateModel(method.getName(), value);
		}
		return model;
	}
	
	protected List<TemplateModel> getChilds() {
		List<TemplateModel> childs = new ArrayList<TemplateModel>();
		for (KbeeMethod canonical : getCanonicalsMap().values()) {
			childs.add(getModel(canonical));
		}
		return childs;
	}
	
	protected TemplateModel getModel(KbeeMethod canonical) {
		TemplateModel model = null;
		if (canonical!=null) {
			Object value = canonical.evaluate(getObject());
			if (value!=null) {
				model = wrap(canonical, value);
			}	
		}
		return model;
	}
	
	protected KbeeMethod getCanonical(String key) {
		return getCanonicalsMap().get(key.toLowerCase());
	}
	
	protected Map<String, KbeeMethod> getCanonicalsMap() {
		if (canonicalsMap == null) {
			canonicalsMap = new HashMap<String, KbeeMethod>();
			for (KbeeMethod canonical : getCanonicals()) {
				canonicalsMap.put(canonical.getName().toLowerCase(), canonical);
			}
		}
		return canonicalsMap;
	}
	
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = new HashSet<KbeeMethod>();
		canonicals.add(new KbeeMethod("Id", "Id"));
		return canonicals;
	}
	
	protected Set<String> keysSet() throws TemplateModelException {
		Set<String> keys = new HashSet<String>();
		keys.addAll(getCanonicalsMap().keySet());
		return keys;
	}
	
	protected PersonMember getMember(Person person) {
		PersonMember personmember = null, usermember = null;
		List<DataSetMember> members = getContentDao().findMembersByEntity(person);
		for (DataSetMember member : members) {
			if (DataSetType.USER.equals(member.getDataSet().getDataSetType())) {
				usermember = (PersonMember)member;
			}
			else {
				personmember = (PersonMember)getContentDao().unproxy(member);
			}
		}
		personmember = personmember!=null ? personmember :usermember;
		return personmember; 
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
}