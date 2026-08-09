package kbee.web.workflow;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePhoto;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;

import kbee.util.logging.Logger;
import kbee.web.user.UserAvatarPanel;

public class TaskCommentPanel extends ModelPanel<WorkflowContext> {
	private static final long serialVersionUID = 1L;

	static private Logger logger = Logger.getLogger(TaskCommentPanel.class.getName());
	
	private List<Activity> history = null;

	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private Boolean has_full_audit = null;
	
	public TaskCommentPanel(String id, IModel<WorkflowContext> model) {
		super(id, model);
	}
	
	@SuppressWarnings("serial")
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer comment = new WebMarkupContainer("comment") {
			public boolean isVisible() {
				Activity currentactivity = getCurrentActivity();
				boolean has_full_audit = hasFullAudit(currentactivity);
				Activity previousactivity = getPreviousActivity();
				return hasSystemComment() || 
					(has_full_audit && previousactivity!=null && 
					previousactivity.getNote()!=null &&
					(previousactivity.getAssignedBy()==null || !previousactivity.getAssignedBy().equals(currentactivity.getUser())));  
			}
		};
		
		Activity activity = getPreviousActivity();
		
		if (getPreviousActivity()!=null && getPreviousActivity().getNote()!=null) {
			User user = activity.getUser();
			if (activity.getStatus().equals(Activity.Status.REASSIGNED)) {
				if (activity.getAssignedBy()!=null) {
					user = activity.getAssignedBy(); 
				}
			}
			comment.add(new UserAvatarPanel("photo", new ObjectModel<User>(user)));
		}
		else 
			comment.add(new InvisiblePhoto("photo"));
		
		
		comment.add(new Label("reassigned-label", getLabel("taskinfo.message.reassignedby")) {
			public boolean isVisible() {
				Activity activity = getPreviousActivity();
				return activity!=null && activity.getStatus().equals(Activity.Status.REASSIGNED);
			}
		});
		
		
		comment.add(new Label("comment-user", new Model<String>() {
			public String getObject() {
				User user = null;
				if (hasSystemComment()) {
					user = getContext().getContent().getLastModifiedUser();
				}
				else {
					Activity activity = getPreviousActivity();
					if (activity==null) return null;
					user = activity.getUser();
					if (activity.getStatus().equals(Activity.Status.REASSIGNED)) {
						if (activity.getAssignedBy()!=null) {
							user = activity.getAssignedBy(); 
						}
					}
				}
				return user!=null ? user.getFirstLastName() : null;
			}
		}));
		
		comment.add(new Label("comment-user-title", new Model<String>() {
			private String getBusinessTitle() {
				try {
					Activity activity = getPreviousActivity();
					User user = activity.getUser();
					if (activity.getStatus().equals(Activity.Status.REASSIGNED)) {
						if (activity.getAssignedBy()!=null) {
							user = activity.getAssignedBy(); 
						}
					}
					Person person = getContentDao().findUserProfileByUser(user).getPerson();
					return person.getBusinessTitle()!=null?person.getBusinessTitle():"";
				} 
				catch (Exception e) {
					logger.error(e);
					return e.getClass().getSimpleName();
				}
			}
			@Override
			public String getObject() {
				return getBusinessTitle();
			}
			
		}) { 
			
			@Override
			public boolean isVisible() {
				try {
					Activity activity = getPreviousActivity();
					if (activity==null) return false;
					User user = activity.getUser();
					if (activity.getStatus().equals(Activity.Status.REASSIGNED)) {
						if (activity.getAssignedBy()!=null) {
							user = activity.getAssignedBy(); 
						}
					}
					Person person = getContentDao().findUserProfileByUser(user).getPerson();
					if (person.getBusinessTitle()!=null && person.getBusinessTitle().length()>0)
						return true;
					return false;
				} 
				catch (Exception e) {
					return true;
				}
			}
		});
		
		comment.add((new Label("comment-time", new Model<String>() {
			public String getObject() {
				OffsetDateTime time = hasSystemComment() ? 
					getContext().getProcess().getStartTime() : 
					getPreviousActivity().getEndTime();
				return format(time);
			}
		})).setEscapeModelStrings(false));
		
		comment.add( (new Label("comment-text", new Model<String>() {
			public String getObject() {
				String note = null;
				if (hasSystemComment()) {
					note = getContext().getNote();
				}
				else {
					Activity activity = getPreviousActivity();
					note = activity.getNote();
				}
				if (note!=null) {
					note = note.replace("\r\n\r\n\r\n", "<br /><br />");
					note = note.replace("\r\n", "<br />");
					note = note.replace("\n", "<br />");
				}
				return note;
			}
		})).setEscapeModelStrings(false));
		
		add(comment);
	}
	
	public List<Activity> getHistory() {
		if (this.history!=null) 
			return this.history;
		this.history = new ArrayList<Activity>();
		this.history.addAll(getModel().getObject().getProcess().getActivities());
		if (!this.history.isEmpty() && this.history.get(0).isRunning()) {
			this.history.remove(0);
		}	
		return this.history;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.history = null;
	}
	
	protected String format(OffsetDateTime datetime) {
		return format(datetime, "ago");
	}
	 
	protected String format(OffsetDateTime datetime, String css) {
		if (datetime==null)
			return "";
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());

		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		
		ZonedDateTime zd = ZonedDateTime.ofInstant(datetime.toInstant(), ZoneId.of(zid));
		Session session = Session.get();
		String label =  service.timeElapsed(zd, ZoneId.of(zid), session.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, css);
		
		return label;
	}
	
	protected String format(OffsetDateTime datetime, int formatter) {
		if (datetime==null)
			return "";

		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		String zid = service.getMapZoneIds().get(user.getTimeZone());
		
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		
		Session session = Session.get();
		String label = service.format(datetime, ZoneId.of(zid).getId(), session.getLocale(), formatter);  
		
		return label;
	}
	
	protected Content getContent() {
		return ((KbeeContext)getModel().getObject()).getContent();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected boolean isAuthorizedPrivateNotes() {											
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getContent());
	}
	
	protected boolean hasSystemComment() {
		return getPreviousActivity()==null && getContext().getNote()!=null && getHistory().isEmpty();
	}
	
	private Activity getCurrentActivity() {
		return ((KbeeContext)getModel().getObject()).getCurrentActivity();
	}
	
	private Activity getPreviousActivity() {
		return ((KbeeContext)getModel().getObject()).getPreviousActivity();
	}
	
	private KbeeContext getContext() {
		return ((KbeeContext)getModel().getObject());
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}

	/** 
	 *  if user has access to private area
	 *  if user executed or is recepient of the task 
	 *  
	 */
	private boolean hasFullAudit(Activity activity) {

		if (has_full_audit!=null)
			return has_full_audit.booleanValue(); 
		
		if(activity==null || activity.getUser()==null) {
			has_full_audit = Boolean.valueOf(false);
			return has_full_audit.booleanValue();
		}

		if (role_support) {
			has_full_audit =Boolean.valueOf(true);
			return has_full_audit.booleanValue();
		}
			
		// if user has access to Private Notes
		//
		if (isAuthorizedPrivateNotes()) {
			has_full_audit =Boolean.valueOf(true);
			return has_full_audit.booleanValue();
		}

		// if user has done / is doing the  Activity
		// 
		if (activity.getUser().getId().equals(getSessionUser().getId())) {
			has_full_audit =Boolean.valueOf(true);
			return has_full_audit.booleanValue();
		}
		

		if (activity.isRunning()) {
			
			if (!getHistory().isEmpty()) {
				Activity am = getHistory().get(0);
				if (((KbeeWorkflowActivity)am).getUser().getId().equals(((KbeeWorkflowActivity)activity).getUser().getId())) {
					this.has_full_audit =Boolean.valueOf(true);
					return this.has_full_audit.booleanValue();
				}
				this.has_full_audit =Boolean.valueOf(false);
				return this.has_full_audit.booleanValue();
			}
			else {
				
				this.has_full_audit =Boolean.valueOf(false);
				return this.has_full_audit.booleanValue();
			}
		}
		
		// if User has done previous Activity
		//
		Activity previous_ac = null;
		
		for (Activity am: getHistory()) {
			logger.debug(am.getTask().getName());
			if (((KbeeWorkflowActivity)am).getId().equals( ((KbeeWorkflowActivity)activity).getId())) 
				break;
			previous_ac = am;
		}
		
		if (previous_ac!=null &&  previous_ac.getUser().getId().equals(getSessionUser().getId())) {
			this.has_full_audit =Boolean.valueOf(true);
			return this.has_full_audit.booleanValue();
		}

		this.has_full_audit =Boolean.valueOf(false);
		return this.has_full_audit.booleanValue();
	}
	
}
