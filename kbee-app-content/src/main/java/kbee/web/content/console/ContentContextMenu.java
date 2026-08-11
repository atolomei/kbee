package kbee.web.content.console;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSetMember;


import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.LabelSet;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.AppMonitoringService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Priority;
import com.novamens.workflow.Task;

public class ContentContextMenu extends ContextMenuPanel<Content> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentContextMenu.class.getName());
	
	private String console_name;
	private IModel<User> model_session_user = null;
	private List<IModel<Priority>> priorities_model;
	private Map<Long, List<IModel<LabelMember>>> labels = new HashMap<Long, List<IModel<LabelMember>>>();	
	private boolean eforms = true;

	
	public ContentContextMenu(String id, IModel<Content> model, String name) {
		super(id, model);
		this.console_name=WorkspaceConsole.NAME;
	}

	
	public String getName() {
		return console_name;
	}

	
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			if (this.labels!=null)
				this.labels.forEach((k, v) -> v.forEach(item->item.detach()));
		} catch (Exception e) {
				logger.error(e);
		}
	}
	
	

	protected List<IModel<LabelMember>> getLabelMembers(ContentTemplate ct) {
			
		if (this.labels.containsKey((Long) ct.getId()))
				return this.labels.get((Long) ct.getId());
			
			List<IModel<LabelMember>> xl = new ArrayList<IModel<LabelMember>>();
			 List<ClassifierTemplate> list = ct.getClassifiers(); //getDataSet().getClassifiers();
			 for (ClassifierTemplate ca: list) {
				 if (ca.getClassifier() !=null && ca.getClassifier().getState()==ObjectState.ENABLED && (ca.getClassifier().getDataSet() instanceof LabelSet)) {
					 for (DataSetMember dm: getContentDao().getMembers(ca.getClassifier().getDataSet(), "strvalue")) {
						 if (dm.getState()==ObjectState.ENABLED)
							 xl.add(new ObjectModel<LabelMember>((LabelMember) dm)); 
					 }
				 }
			 }
			Collections.sort(xl, new Comparator<IModel<LabelMember>>() {
				@Override
				public int compare(IModel<LabelMember> a, IModel<LabelMember> b) {
					try { 
						if (a.getObject()!=null && b.getObject().getDisplayName()==null)
							return -1;
						if (b.getObject()!=null && a.getObject().getDisplayName()==null)
							return -1;
						return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName());
					} catch (Exception e) {
						logger.error(e);
						return 0;
					}
				}
			});
			this.labels.put((Long) ct.getId(), xl);
			return this.labels.get((Long) ct.getId());
	}

	/***
	 * 
	 * 
	 */
	protected List<IModel<Priority>> getPriorities(IModel<Content> model) {
		if (this.priorities_model!=null)
			return this.priorities_model;
		this.priorities_model = new ArrayList<IModel<Priority>>();
		this.priorities_model.add(new Model<Priority>(Priority.Standard));
		this.priorities_model.add(new Model<Priority>(Priority.High));
		this.priorities_model.add(new Model<Priority>(Priority.Urgent));
		
		return this.priorities_model; 
	}


	
	protected boolean eforms() {
		return eforms;
	}

	
	protected boolean isPrivateNotes(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(model.getObject());
	}
	
	protected boolean isAuditReadable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isAuditTrailReadable(model.getObject());
	}
	
	protected boolean isWriteable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
	protected boolean isMonitorable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isMonitorable(model.getObject());
	}
	
	protected boolean isTakeable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isTakeable(model.getObject());
	}
	
	protected boolean isTerminable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isTerminable(model.getObject());
	}
	
	protected boolean isDeleteable(IModel<Content> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(model.getObject());
	}
	
	protected boolean isDeleteable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isDeleteable(content);
	}
	
	protected boolean isAdminUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	

	protected KbeeUser getSessionUser() {
		try {
			if (model_session_user != null && model_session_user.getObject() != null)
				return (KbeeUser) model_session_user.getObject();

			User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			model_session_user = new ObjectModel<User>(session_user);
			return (KbeeUser) model_session_user.getObject();
		} catch (Exception e) {
				logger.error(e);
			return null;
		}
	}

	protected UserProfile getSessionUserProfile() {
		return getContentDao().findUserProfileByUser(getSessionUser());
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


	
	protected String getPageUrl(IModel<Content> model) 	{
		Task task = null;
		try {
			WorkflowService workflowService = model.getObject().getService(WorkflowService.class);
			task = workflowService.getTask();
			String url =  "/task/id/";
			url += eforms() ? "v6/" : "";
			url += task.getId().replaceAll("\\s", "-").toLowerCase() + "/" + model.getObject().getId();
			return url;
		} 
		catch (Exception e) {
			logger.error(e, (model!=null && model.getObject()!=null) ? model.getObject().toString()  : "null");
			if (task==null) {
				ServiceLocator.getService( AppMonitoringService.class).attempToReindexContent(model.getObject());
			}
			return "";
		}
		
	}
	

	
}
