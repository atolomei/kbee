package kbee.web.workflow;


import java.time.OffsetDateTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.KbeeWorkflowThreadStatus;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.workflow.Activity;
import com.novamens.workflow.ForkJoinTask;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowThreadStatus;

import kbee.web.command.panel.CommandAttributePanelV5;

public class TaskInfoPanel<T extends Content> extends ModelPanel<WorkflowContext>  {
	
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskInfoPanel.class.getName());
 	


	final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	
	private boolean showall = true;
	
	private List<Activity> history = null;
	
	
	public TaskInfoPanel(IModel<WorkflowContext> workflowmodel) {
		this("task-info", workflowmodel);
	}
	
	public TaskInfoPanel(String id, IModel<WorkflowContext> workflowmodel) {
		this(id, workflowmodel, true);
	}
	

	/**
	 * @param id
	 * @param workflowmodel
	 * @param is_actions_visible
	 */
	public TaskInfoPanel(String id, IModel<WorkflowContext> workflowmodel, boolean isshowall) {
		super(id, workflowmodel);
	
		this.setShowAll(isshowall);
		setOutputMarkupId(true);
		adProcessItems();
		// adContentItems();
		
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.history = null;
	}


	public void setShowAll(boolean b) {
		this.showall=b;
	}
	
	
	public List<Activity> getHistory() {
		if (this.history!=null) 
			return this.history;
		this.history = new ArrayList<Activity>();
		this.history.addAll(getModel().getObject().getProcess().getActivities());
		if (this.history.get(0).isRunning()) {
			this.history.remove(0);
		}	
		return this.history;
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
		String label = service.timeElapsed(zd, ZoneId.of(zid), session.getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, css);
		
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

	protected boolean isAuthorizedPrivateNotes() {											
		return ServiceLocator.getService(ContentSystemSecurityService.class).isPrivateEnabled(getContent());
		
	}

	 

	
	@SuppressWarnings("unchecked")
	protected T getContent() {
		return (T)((KbeeContext)getModel().getObject()).getContent();
	}

	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	
	
	private void adProcessItems() {

		String wk;
		
		if (getModelObject().getTime()==null) {
			wk = getLabelString("pending");
		}
		else {
			User user = ((KbeeContext)getModelObject()).getUser();
			wk= (user!=null ? user.getFirstLastName() : "null");
		}
		
		
		String sta;
		
		if (getModelObject().getTime()==null)
			sta =  new StringResourceModel("not-started", TaskInfoPanel.this, null).getString();
		else
			sta = format(getModelObject().getTime());
			
		List<KeyValue<String>> list = new ArrayList<KeyValue<String>>();
		list.add( new KeyValue<String>(getLabel("procedure.name").getObject(), getModelObject().getProcedure().getDisplayName() ));
		list.add( new KeyValue<String>(getLabel("task.process.started").getObject(), format(getModelObject().getProcess().getStartTime()) ));
		
		
		OffsetDateTime due_date=getModelObject().getDueDate();
		
		//due_date= OffsetDateTime.now().plusDays(3); 
		
		if (due_date!=null) {								
			list.add( new KeyValue<String>(getLabel("due-date").getObject(), 
						format(due_date)
					));
		}
		

		
		
		if (showall) {
				if (getModelObject().getTask() instanceof ForkJoinTask) {
					String text = "<ul>";
					int t = 0;
					for (WorkflowThreadStatus thread : ((KbeeContext)getModelObject()).getThreads()) {
						Content content = ((KbeeWorkflowThreadStatus)thread).getContent();
						KbeeContext context = content!=null ? (KbeeContext)content.getService(WorkflowService.class).getContext() : null;
						Activity activity = context!=null ? context.getCurrentActivity() : null;
						text += "<li style=\"width: 100%;";
						text += t>0 ? "margin-top:10px;\">" : "\">"; 
						text += "<span style=\"display:block;\">" + thread.getThread().getName() + "</span>";
						if (activity!=null) {
							String from = format(activity.getStartTime());
							text += "<span style=\"display:block;\">" + activity.getTask().getDisplayName() + "</span>";
							text += "<span style=\"display:block;\">" + thread.getStatus() + " "+ from + " </span>";
							text += "<span style=\"display:block;\">" + activity.getUser().getDisplayName() + "</span>";
						}
						else {
							text += "<span style=\"display:block;\">" + thread.getStatus() + "</span>";
						}
						if (thread.getReason()!=null) {
							text += "<span style=\"display:block;\">" + thread.getReason().getLabel() + "</span>";
						}
						text += "</li>";
						t++;
					}
					text += "</ul>";
					list.add( new KeyValue<String>(getLabel("threads").getObject(), text));
				}
				list.add( new KeyValue<String>(getLabel("task").getObject(), getModelObject().getTask()!=null ? getModelObject().getTask().getName() : ""));
				if (((KbeeContext)getModelObject()).getThread()!=null) {
					list.add( new KeyValue<String>(getLabel("thread").getObject(), ((KbeeContext)getModelObject()).getThread()));
				}	
				list.add( new KeyValue<String>(getLabel("workspace").getObject(), wk));
				list.add( new KeyValue<String>(getLabel("property.task-started").getObject(), sta));
				list.add( new KeyValue<String>(getLabel("priority").getObject(), getModelObject().getPriority().getLabel(getSessionUser().getLocale())));				
		  		
				if (getModelObject().getReason()!=null)
					list.add( new KeyValue<String>(getLabel("task-reason").getObject(),getModelObject().getReason().getLabel()));
				
				if (getModelObject().getDueDate()!=null) {
					String dd;
					try {
						OffsetDateTime duedate = getModelObject().getDueDate();
						if (duedate!=null)
								dd= format(duedate);
						else
							dd= "";
					} 
					catch (Exception e) {
						logger.error(e);
						dd= e.getClass().getSimpleName();
					}
					list.add( new KeyValue<String>(getLabel("property.duedate").getObject(), dd));
				}
		
				Activity activity = getPreviousActivity();
				
				if (activity!=null) {
					String ptn;
					try {
						 ptn = activity.getTask().getName();
						} catch (Exception e) {
							logger.error(e);
							ptn = e.getClass().getSimpleName();
						}
					String ptu;
					try {
						ptu = getContentDao().findUserProfileByUser(activity.getUser()).getPerson().getFirstLastName();
						} catch (Exception e) {
							logger.error(e);
							ptu = e.getClass().getSimpleName();
						}
					list.add( new KeyValue<String>(getLabel("property.task-previous").getObject(), ptn ));
					list.add( new KeyValue<String>(getLabel("previous-task-user").getObject(), ptu ));
				}
		}
		
		
		list.sort(new Comparator<KeyValue<String>>() {
			@Override
			public int compare(KeyValue<String> o1, KeyValue<String> o2) {
				return o1.getKey().toString().compareToIgnoreCase(o2.getKey().toString());
			}
		});
		
		
		List<Panel> panels = new ArrayList<Panel>();
		for ( KeyValue<String> kv:list) 
			panels.add(new CommandAttributePanelV5("item", new Model<String>(kv.getKey().toString()), new Model<String>(kv.getValue())));
		
		 add(new ListView<Panel>("process", panels) {
	            private static final long serialVersionUID = 1L;
	            protected void populateItem(ListItem<Panel> item) {
	                item.setOutputMarkupId(true);
	                item.add(item.getModelObject());
	                item.setVisible(item.getModelObject().isVisible());
	            }
	        });
	}
	
	private Activity getPreviousActivity() {
		return ((KbeeContext)getModel().getObject()).getPreviousActivity();
	}

}
