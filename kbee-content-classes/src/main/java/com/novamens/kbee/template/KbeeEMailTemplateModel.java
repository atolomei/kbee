package com.novamens.kbee.template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.PortalDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.portal6.model.Site;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;

import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import kbee.util.logging.Logger;

public class KbeeEMailTemplateModel extends KbeeObjectTemplateModel {
	
	private static Logger logger = Logger.getLogger(KbeeEMailTemplateModel.class.getName());
	
	private Person sender, receiver;
	private Content content;
	
	private Map<String, Object> models = new HashMap<String, Object>();;
	private Map<String, Object> parameters = new HashMap<String, Object>();
	private Map<String, String> macros = new HashMap<String, String>();
	
	
	private static String _default_noreply = null;
	
	public KbeeEMailTemplateModel() {
		addAppContextMacros();
		addWorkflowMacros();
		addGeneralMacros();
		addContentMacros();
		addParametersMacros();
		addDefaultMacros();
	}
	
	public KbeeEMailTemplateModel(Person sender, String to, Map<String, Object> parameters, Content content) {
		setSender(sender);
		setContent(content);
		
		parameters.put("to", to);
		setParameters(parameters);
		
		addAppContextMacros();
		addWorkflowMacros();
		addGeneralMacros();
		addContentMacros();
		addParametersMacros();
		addDefaultMacros();
	}
	
	public void setSender(Person person) {
		this.sender = person;
	}
	
	public Person getSender() {
		return sender;
	}
	
	public void setReceiver(Person person) {
		this.receiver = person;
	}
	
	public Person getReceiver() {
		return receiver;
	}
	
	public void setContent(Content content) {
		this.content = content;
		addGeneralMacros();
	}
	
	public Content getContent() {
		return content;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void setModel(String key, Object value) {
		if (value instanceof KbeeObjectTemplateModel) {
			((KbeeObjectTemplateModel)value).setParentNode(this);
		}
		models.put(key, value);
	}
	
	public Map<String, Object> getModels() {
		return models;
	}
	
	@Override
	public Object getObject() {
		return this;
	}
	
	protected Map<String, String> getMacros() {
		return macros;
	}
	
	public TemplateModel get(String key) throws TemplateModelException {
		TemplateModel model = null;
		if (getModels().containsKey(key)) {
			model = wrap(new KbeeMethod(key, null), getModels().get(key));
		}
		else
		if (getMacros().containsKey(key)) {
			model = new KbeeCanonicalTemplateModel(key, getMacros().get(key));
		}
		else {
			model = super.get(key);
		}
		return model;
	}
	
	public String escape(String template) {
		
		if (template==null)
			return  null;
		
		for (String macro : getMacros().keySet()) {
			String escaped = null;
			if (macro.contains("-")) {
				escaped = macro.replace("-", "\\-");
				template = template.replace(macro, escaped);
				macro = escaped;
			}
			if (macro.contains(".")) {
				escaped = macro.replace(".", "\\.");
				template = template.replace(macro, escaped);
			}		
		}
		return template;
	}
	
	@Override
	public String getAsString() {
		return getContent().getTitle();
	}
	
	@Override
	public String getNodeName() throws TemplateModelException {
		return "email";
	}
	
	@Override
	public String getNodeType() throws TemplateModelException {
		return "email";
	}
	
	@Override
	public TemplateCollectionModel values() throws TemplateModelException {
		Set<TemplateModel> keys = new HashSet<TemplateModel>();
		return new SimpleCollection(keys, null);
	}
	
	@Override
	protected List<TemplateModel> getChilds() {
		List<TemplateModel> childs = super.getChilds();
		for (String parameter : getParameters().keySet()) {
			Object value = getParameters().get(parameter);
			String stringvalue = value!=null ? value.toString() : null;
			if (stringvalue!=null) {
				childs.add(new KbeeCanonicalTemplateModel(parameter, stringvalue));
			}
		}
		for (String key : getModels().keySet()) {
			childs.add(wrap(new KbeeMethod(key, null), getModels().get(key)));
		}
		return childs;
	}
	
	@Override
	protected TemplateModel wrap(KbeeMethod method, Object value) {
		TemplateModel model = null;
		//if (value!=null && value instanceof Content) {
		//	model = new KbeeContentTemplateModel(getContent());
		//}
		//else {
		model = super.wrap(method, value);
		//}
		return model;
	}
	
	protected void addMacro(String key, String value) {
		macros.put(key, value);
	}
	
	@Override
	protected Set<KbeeMethod> getCanonicals() {
		Set<KbeeMethod> canonicals = super.getCanonicals();
		canonicals.add(new KbeeMethod("sender", "sender"));
		canonicals.add(new KbeeMethod("receiver", "receiver"));
		canonicals.add(new KbeeMethod("content", "content"));
		return canonicals;
	}
	
	protected Domain getDomain() {
		Content content = getContent();
		if (content==null) {
			return null;
		}
		Domain domain = content.getDomain();
		return domain;
	}
	
	protected void addGeneralMacros() {
		try {
			
			Domain domain = getDomain();
			
			if (domain==null) {
				return;
			}
			
			String mytasksurl = getServerUrl(domain) + "/mytasks";
			String library_link = getServerUrl(domain) + "/content";
			String pending_url  = getServerUrl(domain) + "/pendingtasks";
			String server_url  = getServerUrl(domain);
			
			addMacro("domain-name", domain.getOrganization()!=null?domain.getOrganization():domain.getName());
			
			String durl = getServerUrl(domain);
			addMacro("domain-url", durl);
			
			addMacro("server-url", server_url);
			addMacro("library-url", library_link);
			addMacro("library-link", library_link);
			addMacro("my-tasks-link", mytasksurl);
			addMacro("my-tasks-url", mytasksurl);
			addMacro("pending-tasks-url", pending_url);
		} 
		catch (Exception e) {
			addMacro("error", e.getClass().getName()+ " " + e.getMessage());
			logger.error(e);
		}
	}
	
	protected void addContentMacros() {
		try {
			Content content = getContent();
			
			if (content==null) {
				logger.debug("content is null");
				return;
			}
			addMacro("file-title", (content.getDisplayName()!=null?content.getDisplayName():""));
			addMacro("title", (content.getDisplayName()!=null?content.getDisplayName():""));
			addMacro("domain-name", (content.getDomain().getOrganization()!=null?content.getDomain().getOrganization():content.getDomain().getName()));
			addMacro("file-library-url", getServerUrl(content.getDomain())  + "/" +  content.getClassCode()	+ "/" +  String.valueOf(content.getOId()));
			
			StringBuilder str = new StringBuilder();
			for (String s: content.getMetadataAsList()) {
				if (str.length()>0)
					str.append("<br/>");
				str.append(s);
			}
			addMacro("file-attributes", str.toString());
			addMacro("file-metadata", str.toString());
			addMacro("file-content-classifier", content.getContentTypeClassificationAsString());
			
			for (Site site: getPortalDao().getSitesPublic(content.getDomain())) {
				if (site.getState()==ObjectState.ENABLED && !site.isExternal()) {
					String siteurl = getServerUrl(content.getDomain()) + "/portal/"+ site.getUrl() + "/doc/" +  String.valueOf(content.getOId());
					addMacro("portal-"+site.getKey()+"-url", siteurl);
				}	
			}
			
			addAttributesMacros();
		} 
		catch (Exception e) {
			addMacro("error", e.getClass().getName()+ " " + e.getMessage());
			logger.error(e);
		}
	} 
	
	protected void addAttributesMacros() {
		Content content = getContent();
		
		if (content==null) {
			logger.debug("content is null");
			return;
		}
		try {
			Map<String, List<String>> classification = content.getClassificationAsMapString();
			
			String prefix ="file-attribute.";
			for (Entry<String, List<String>> entry: classification.entrySet()) {
				StringBuilder str = new StringBuilder();
				for (String s: entry.getValue()) {
					if (str.length()>0)
						str.append(", ");
					str.append(s);
				}
				String key_lc= prefix+ entry.getKey().toLowerCase().replace("$", "").replace("{", "").replace("}","");
				addMacro(key_lc, str.toString());
			}
							
			addMacro("file-console-subtitle", content.getService(ContentService.class).getConsoleSubtitle());
			addMacro("file-portal-subtitle", content.getService(ContentService.class).getPortalSubtitle());
		} 
		catch (Exception e) {
			addMacro("content-template-error", e.getClass().getName()+ " " + e.getMessage());
			logger.error(e);
		}
	}
	
	protected void addAppContextMacros() {
		try {
			if (getSender()!=null)	{
				addMacro("from-displayname", getSender().getFirstLastName());
				addMacro("person-displayname", getSender().getFirstLastName());
				addMacro("person-username", getSender().getProfile(UserProfile.class).getUser().getUserName());
				addMacro("person-email-address", getSender().getEmail());
				addMacro("publisher", getSender().getFirstLastName());
			}
			if (getReceiver()!=null)		{
				addMacro("receiver", getReceiver().getFirstLastName());
				addMacro("subscriber", getReceiver().getFirstLastName());
				addMacro("username", getReceiver().getProfile(UserProfile.class).getUser().getUserName());
			}
		} 
		catch (Exception e) {
			addMacro("context-error", e.getClass().getName()+ " " + e.getMessage());
			logger.error(e);
		}
	}
	
	protected void addWorkflowMacros() {
		if (getContent()==null) {
			return;
		}
		KbeeContext context = (KbeeContext)getContent().getService(WorkflowService.class).getContext();
		if (context==null || context.getProcedure()==null) {
			logger.debug("addWorkflowMacros: workflow context is null");
			return;
		}
		try {
			addMacro("procedure", context.getProcedure().getDisplayName());
			addMacro("task", context.getTask().getDisplayName());
			addMacro("task-name", context.getTask().getName());
			
			Content content=((KbeeContext) context).getContent();
				
			Activity cu= context.getCurrentActivity();
			
			if (cu!=null) {
				
				addMacro("task-person-name",  (cu.getUser()!=null?cu.getUser().getFirstLastName():"[null]"));
				
				addMacro("task-start-date", 
						ServiceLocator.getService(DateTimeService.class).getDateDisplayString(
								context.getTime(), 
						        (cu.getUser()!=null?cu.getUser().getLocale() :  content.getDomain().getLocale())
					   )
				);
			}
				
			String task_url = content.getService(UrlService.class).getTaskUrl();
				
			Activity a = context.getPreviousActivity();
				
			String note;
			if (a!=null) {
				note= (a.getNote()!=null?
						a.getNote().replace("\t\n", "<br />").replace("\n", "<br/>"):
						"");
				
				addMacro("previous-task", a.getTask().getDisplayName());
				addMacro("previous-task-name",  a.getTask().getDisplayName());
				addMacro("previous-task-person-name",  a.getUser().getFirstLastName());
				String noteauthor = a.getAssignedBy()!=null ? a.getAssignedBy().getFirstLastName() : a.getUser().getFirstLastName();
				addMacro("task-note-author",  noteauthor);
			}
			else {
				note="";
			}
				
			addMacro("task-url", task_url);
			if (!"".equals(note)) {
				addMacro("task-note", note);
			}
			addMacro("comment", note);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void addDefaultMacros() {
		addMacro("domain-noreply", getNoReplyEmailAddress()); 
		addMacro("from", getNoReplyEmailAddress());
		addMacro("service-noreply", getNoReplyEmailAddress());
		addMacro("application", ServiceLocator.getService(BrandingService.class).getApplicationShortName());
		addMacro("application-name", ServiceLocator.getService(BrandingService.class).getApplicationShortName());
		addMacro("application-fullname", ServiceLocator.getService(BrandingService.class).getApplicationName());
		addMacro("training-url", ServiceLocator.getService(BrandingService.class).getTrainingUrl());
	}
	
	public String getNoReplyEmailAddress() {
		if (_default_noreply!=null)
			return _default_noreply;
		synchronized (this) {		
			_default_noreply = ServiceLocator.getService(BrandingService.class).getNoReplyEmailAddress();
			logger.debug(_default_noreply);
		}
		return _default_noreply;
	}
	
	protected void addParametersMacros() {
		try {
			if (getParameters()!=null)	{
				for (String key : getParameters().keySet()) {
					Object value = getParameters().get(key);
					addMacro(key, value!=null ? value.toString() : "");
				}
			}
		} 
		catch (Exception e) {
			addMacro("parameter-error", e.getClass().getName()+ " " + e.getMessage());
			logger.error(e);
		}
	}
	
	private String getServerUrl(Domain domain) {
		return domain.getService(UrlService.class).getServerUrl();
		
		//if (domain.getName().equals("kbee"))
		//	return servername + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
		//return vanity_server.trim().replace("${domain}", domain.getName()) + (vanity_port.length()==0 || vanity_port.equals("80") ? "": (":"+vanity_port));
	}
	
	protected PortalDao getPortalDao() {
		return (PortalDao)ServiceLocator.getService(BeansService.class).getBean("portalDao");
	}
}